from fractions import Fraction
import math
import sys


# Count decimal digits so we can decide when recursion is worth it.
# This is also how we know if a number is small enough to just multiply normally.
def digit_count(value: int) -> int:
    # Edge case where, if the number is 0, it counts as 1 digit because
    # it should behave like a one-block number rather than a special shape.
    if value == 0:
        return 1
    return len(str(abs(value)))


# Break the number into equally sized base-10 chunks from right to left.
# Each chunk becomes one coefficient of the polynomial "f(t)", where
# evaluating at t = base recreates the original integer exactly.
def split_into_chunks(value: int, chunk_digits: int, parts: int) -> list[int]:
    # Splitting into chunks of "chunk digits"
    base = 10 ** chunk_digits
    chunks: list[int] = [] # holds coefficients

    # Repeated division peels off the least-significant chunk first,
    # looping based on how many chunks we want to break the number into.
    # Which is exactly the coefficient order we want for polynomial work.
    for _ in range(parts):
        # Get the rightmost chunk of the number.
        chunks.append(value % base)
        # Break off the rightmost chunk from the original num.
        # Shift it over for the next loop iteration.
        value //= base

    return chunks


# Evaluate a polynomial given by chunk coefficients at a chosen point.
# This converts a number in base B into the same coefficients viewed as a polynomial.
# So now that the numbers are represented as lists of coefficients (like said polynomial),
# we can plug each point (t) into our equations (evaluating at said chosen point).
def evaluate_polynomial(coefficients: list[int], point: int) -> int:
    # Set up running total and current power of "x"
    total = 0
    power_of_point = 1

    # We build the value term by term so the role of each chunk stays
    # visible: coefficient_i contributes coefficient_i * point^i.
    # Basically, we're looping through the chunked number from right to left.
    for coefficient in coefficients:
        # First we multiply the coefficient by the current power of x and
        # add it to the total.
        total += coefficient * power_of_point
        # Increment power of "x" for next chunk
        power_of_point *= point

    return total


# Gaussian elimination: solve system of equations (using Fractions instead
# of floats here to avoid rounding issues).
def solve_linear_system(matrix: list[list[Fraction]],
                        rhs: list[Fraction]) -> list[Fraction]:
    size = len(rhs)

    # Gaussian elimination turns the matrix into "upper-triangular" form.
    # We swap in a non-zero pivot when needed so division is always valid.
    for pivot_index in range(size): # Iterate down the diagonal of the matrix
        pivot_row = pivot_index
        # First find a row that doesn't have a zero in the current column to avoid dividing by zero.
        while pivot_row < size and matrix[pivot_row][pivot_index] == 0:
            pivot_row += 1

        if pivot_row == size:
            raise ValueError("Interpolation matrix is singular.")

        #  Next swap the current row with our valid pivot row.
        if pivot_row != pivot_index:
            matrix[pivot_index], matrix[pivot_row] = matrix[pivot_row], matrix[pivot_index]
            rhs[pivot_index], rhs[pivot_row] = rhs[pivot_row], rhs[pivot_index]

        pivot_value = matrix[pivot_index][pivot_index]

        # Normalizing the pivot row makes the later subtraction steps
        # simpler, because each pivot becomes exactly 1 before elimination.
        # Do this by dividing the whole row by the pivot number.
        for column in range(pivot_index, size):
            matrix[pivot_index][column] /= pivot_value
        rhs[pivot_index] /= pivot_value

        # "Forward elimination": zeros out everything below our pivot 1.
        for row in range(pivot_index + 1, size):
            factor = matrix[row][pivot_index]
            if factor == 0:
                continue

            for column in range(pivot_index, size):
                matrix[row][column] -= factor * matrix[pivot_index][column]
            rhs[row] -= factor * rhs[pivot_index]

    solution = [Fraction(0) for _ in range(size)]

    # "Back substitution": work backwards from bottom up to solve for the actual unknown variables.
    for row in range(size - 1, -1, -1):
        remaining = rhs[row]

        for column in range(row + 1, size):
            remaining -= matrix[row][column] * solution[column]

        solution[row] = remaining

    return solution


# Interpolate the product polynomial h(t) from its sampled values.
# Because we multiplied polynomials together, we only know the results of the new polynomial
# at specific points. We need to go back to figure out what the new polynomial's coefficients are.
def interpolate_coefficients(points: list[int], values: list[int]) -> list[int]:
    matrix: list[list[Fraction]] = []
    rhs: list[Fraction] = []

    # Row i stores 1, x_i, x_i^2, ... so multiplying the row by the
    # unknown coefficient vector yields h(x_i), the sampled value there.
    for point, value in zip(points, values): # loop over sample points
        row = [Fraction(1)]

        # Build Vandermonde matrix. Each row looks like [1, x, x^2, x^3...].
        # Do this to get a matrix that represents the system of equations so
        # that we can easily pass it into the Gaussian system solver.
        for _ in range(1, len(points)):
            row.append(row[-1] * point)

        matrix.append(row)
        rhs.append(Fraction(value))

    # Plug this matrix into the system solver
    rational_solution = solve_linear_system(matrix, rhs)
    integer_solution: list[int] = []

    # The true polynomial coefficients are integers in this setting.
    # If a denominator survives, something in the math is wrong.
    # (If denominator is not 1, it's not a whole number)
    for coefficient in rational_solution:
        if coefficient.denominator != 1:
            raise ValueError("Interpolation produced a non-integer coefficient.")
        integer_solution.append(coefficient.numerator)

    return integer_solution


# Rebuild the final integer by evaluating the coefficient list at t = base.
# We have our final polynomial coefficients. Now, we just plug our original
# base back in to get the polynomial back into standard integer form.
def recombine_from_base(coefficients: list[int], base: int) -> int:
    total = 0
    base_power = 1

    # The product polynomial may have more terms than either input,
    # because multiplying two degree-(k - 1) polynomials doubles degree.
    for coefficient in coefficients:
        # Plugs the base in. If the base was 100, for example,
        # this does C_0(1) + C_1(100) + C_2(10000)...
        total += coefficient * base_power
        # Incrememnt power for next iteration.
        base_power *= base

    return total


# The recursive worker assumes non-negative inputs so the splitting and
# evaluation steps stay focused on magnitude, while sign is handled once
# by the public wrapper around the algorithm.
def _toom_cook_nonnegative(x: int, y: int, parts: int, threshold: int) -> int:
    # Zero times zero is zero.
    if x == 0 or y == 0:
        return 0

    # BASE CASE: small inputs aren't worth it, so we fall back to regular multiplication.
    if min(digit_count(x), digit_count(y)) <= threshold:
        return x * y

    max_digits = max(digit_count(x), digit_count(y))
    # How many digits per chunk:
    chunk_digits = max(1, math.ceil(max_digits / parts))
    base = 10 ** chunk_digits

    # Call splitting function to turn "x" and "y" into polynomials.
    x_chunks = split_into_chunks(x, chunk_digits, parts)
    y_chunks = split_into_chunks(y, chunk_digits, parts)
    # Evaluate both "x" and "y" polynomials at those specific points.
    # Use symmetric points [0, 1, -1, 2, -2, ..., k-1, -(k-1)] so that
    # max |t| = k-1, keeping evaluated values small enough to converge.
    sample_points = [0]
    for i in range(1, parts):
        sample_points.append(i)
        sample_points.append(-i)

    x_values: list[int] = []
    y_values: list[int] = []

    # Each sample point turns one larger multiplication into a product of
    # smaller evaluated numbers. Basically, we multiply the evaluated points
    # and if they are still big, break them up again (recurse).
    for point in sample_points:
        x_values.append(evaluate_polynomial(x_chunks, point))
        y_values.append(evaluate_polynomial(y_chunks, point))

    sampled_products: list[int] = []

    # Recursing on the evaluated values computes h(point) for every sample.
    # Once we know enough of these values, interpolation gets h(t).
    for x_value, y_value in zip(x_values, y_values):
        # Evaluated values at negative points can be negative; strip sign
        # before passing to the non-negative engine and restore it after.
        sign = -1 if (x_value < 0) ^ (y_value < 0) else 1
        product = _toom_cook_nonnegative(abs(x_value), abs(y_value), parts, threshold)
        sampled_products.append(sign * product)

    # Use Gaussian system solver to get combined polynomial.
    coefficients = interpolate_coefficients(sample_points, sampled_products)
    # Collapse final polynomial back into a normal integer.
    return recombine_from_base(coefficients, base)


# Choose how many parts to split into, then let pass to the engine for the recursive polynomial stuff.
def toom_cook_multiply(x: int, y: int, parts: int = 3, threshold: int = None) -> int:
    if threshold is None:
        threshold = parts
    if parts < 2:
        raise ValueError("Toom-Cook needs at least 2 parts.")

    # Sign is easier to do outside the recursion, because every
    # internal call can then focus on the non-negative stuff.

    # If only one of the numbers is negative, the final answer must
    # be negative. Otherwise, it's positive.
    sign = -1 if (x < 0) ^ (y < 0) else 1
    # Pass the absolute value of the two numbers into the engine.
    result = _toom_cook_nonnegative(abs(x), abs(y), parts, threshold)
    # Apply the sign to the answer afterward.
    return sign * result


def solve():
    input_data = sys.stdin.read().split()

    if not input_data:
        return

    k = int(input_data[0])
    a = int(input_data[1])
    b = int(input_data[2])
    result = toom_cook_multiply(a, b, parts=k)

    print(result)

if __name__ == "__main__":
    solve()
