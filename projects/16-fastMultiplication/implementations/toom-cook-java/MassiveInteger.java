public class MassiveInteger{
    private static final long BASE = 1000000000L;
    /*
    Custom class to store integers that are much to big to be longs
    The array of ints itself (not the individual ints within the array)
    is stored in little endian form
     
    array of ints will always store magnitude:
    sign as boolean flag 
    */

    private int[] contents;
    private boolean positive;

    public MassiveInteger(String inputStr){
        /*
        instantiate and convert from string
        little endian
        */
        int digitLen = inputStr.length();

        //if string starts with -, makes MassiveInt negative and fixes sizing
        if (inputStr.startsWith("-")){
            this.positive = false;
            inputStr = inputStr.substring(1,digitLen);
            digitLen = inputStr.length();
        }
        else{
            this.positive = true;
        }

        //each element of int array stores up to 10^9
        //max base of 10 that fits in int
        int contentsLen = (digitLen +8 ) / 9;
        this.contents = new int[contentsLen];

        int contentsIdx = 0;
        int stringIdx = digitLen-1; 

        //parse string backwards, cuz little endian
        while(stringIdx>=8){
            contents[contentsIdx] = Integer.parseInt(inputStr.substring(stringIdx-8,stringIdx+1));
            contentsIdx++;
            stringIdx-=9;
        }

        //leftovers
        if (stringIdx>=0){
            contents[contentsIdx]=Integer.parseInt(inputStr.substring(0,stringIdx+1));
        }

    }

    public MassiveInteger(int[] contents, boolean positive){
        //instantiate
        this.contents = contents;
        this.positive = positive;
    }

    public static MassiveInteger createShallowCopy(MassiveInteger massiveInteger){
        /*
        helpful for dealing with negative signs
        this may have been completely unneeded
         */
        return new MassiveInteger(massiveInteger.contents,massiveInteger.positive);
    }


    public int length(){
        return this.contents.length;
    }

    public boolean isPositive(){
        return this.positive;
    }

    public void flipSign(){
        this.positive = !this.positive;
    }

    public void trim(){
        /*
        trim leading 0's if needed
        also returns sign to positive if MassiveInt is just 0
         */
        int i = this.contents.length-1;
       

        if (i==0 && this.contents[0]==0){
            this.positive=true;
            return;
        }

        if (this.contents[i]!=0){
            return;
        }

        while (this.contents[i]==0 && i>0){
            i--;
        }

        int newContentsLen = i+1;
        int[] newContents = new int [newContentsLen];

        for (int j = 0; j<newContentsLen;j++){
            newContents[j]=this.contents[j];
        }
        this.contents=newContents;
    }

    public int getMagnitude(MassiveInteger b){
        /*
        helper method for comparing magnitude
        returns 1 if this is greater,
        -1 if b is greater
        0 if equal
        */
        int thisLen = this.contents.length;
        int bLen = b.contents.length;

        if (thisLen > bLen) return 1;
        if (thisLen < bLen) return -1;

        for (int i = thisLen - 1; i >= 0; i--) {
        if (this.contents[i] > b.contents[i]) return 1;
        if (this.contents[i] < b.contents[i]) return -1;
    }

    return 0;
    }
     
    public MassiveInteger add(MassiveInteger b){
        /*        
        add two massiveIntegers
        
        */

        //if one but not both signs are negative, separate logic is needed;
        //call upon subtract method
        if (this.isPositive() && !b.isPositive()){
            MassiveInteger bMag = createShallowCopy(b);
            bMag.flipSign();
            return this.subtract(bMag);
        }
        if (!this.isPositive() && b.isPositive()){
            MassiveInteger thisMag = createShallowCopy(this);
            thisMag.flipSign();
            return b.subtract(thisMag);
        }
        
        //only have to check sign of 1 operand as different signs already accounted for
        boolean resultPos = this.isPositive() ? true : false; 

        //max result size is the biggest operand size + 1 for overflow
        int maxResultSize = (Math.max(this.contents.length, b.contents.length) + 1);
        int[] resultContents = new int[maxResultSize];

        long carry = 0;

        //core addition algo
        //as you will see in subsequent methods,
        //we cast to longs during intermediate operations
        //to prevent overflow
        //i cannot make peace with the fact that java doesnt have unsigned ints >:(
        for (int i = 0; i < maxResultSize - 1; i++){
            long thisVal = (i<this.contents.length) ? this.contents[i] : 0;
            long bVal = (i<b.contents.length) ? b.contents[i] : 0;

            long intermediateRes = thisVal+bVal+carry;

            resultContents[i]=(int) (intermediateRes%BASE);
            carry = intermediateRes / BASE;
        }

        /* 
        since the max result size is at least one place val greater than 
        either input, we only have to place, not add, the carry (if carry != 0 at all)

        */
        if (carry!=0){
            resultContents[maxResultSize - 1]=(int)carry;
        }

        //you will also see trim at the ends of other methods
        //should have just put that logic inside of the constructor
        //o well
        MassiveInteger resInt = new MassiveInteger(resultContents,resultPos);
        resInt.trim();
        return resInt;
    }


    public MassiveInteger subtract(MassiveInteger b){
        /*
        performs subtraction
        checks if signs suggest addition is underlying operation,
        if not, performs subtraction on the larger magnitude MasInt
        guarenteeing borrowing can always happen
        records signs
         */

        //similar to in addition, checks if it makes more sense to reformulate as the other one
        if (this.isPositive() != b.isPositive()){
            MassiveInteger bMag = createShallowCopy(b);
            bMag.flipSign();
            return this.add(bMag);
        }

        //we want to subtract the 'bigger' one from the 'smaller' one
        //we keep track of the affect of any invertion of ordering on the sign
        int magnitudeFlag = this.getMagnitude(b);

        if (magnitudeFlag==0){
            return new MassiveInteger("0");
        }

        MassiveInteger greaterMagInt = (magnitudeFlag > 0) ? this : b;
        MassiveInteger lesserMagInt = (magnitudeFlag > 0) ? b : this;

        int[] resultContents = new int[greaterMagInt.contents.length];
        long borrow = 0;

        //core subtraction algorithm
        for(int i = 0; i<greaterMagInt.contents.length;i++){
            long gMagIntVal = greaterMagInt.contents[i];
            long lMagIntVal = (i<lesserMagInt.contents.length) ? lesserMagInt.contents[i] : 0;

            long dif = gMagIntVal - lMagIntVal - borrow;

            if (dif<0){
                dif+=BASE;
                borrow = 1;
            }
            else{
                borrow=0;
            }
            resultContents[i] = (int)dif;
        }

        //this line actually accounts both for if we "switched" our operands (the ?) 
        //and if the original values were both negative
        boolean resultPos =  (magnitudeFlag > 0) ? this.isPositive() : !this.isPositive();

        MassiveInteger resInt = new MassiveInteger(resultContents,resultPos);
        resInt.trim();
        return resInt;
    }
    


    public MassiveInteger getLimbs(int from, int to){
        /*
        method to 'split up' MassiveInt,
        as is needed by toom algorithm
        */
        int actualTo = Math.min(to, this.contents.length);
        int len = Math.max(0, actualTo - from);
        int[] result = new int[Math.max(1, len)];
        for (int i = 0; i < len; i++){
            result[i] = this.contents[from + i];
        }
        MassiveInteger resInt = new MassiveInteger(result, true); // always positive; sign handled by caller
        resInt.trim();
        return resInt;
    }

    

    public MassiveInteger schoolbookMultiply(MassiveInteger b){

        boolean positiveRes = (this.positive==b.isPositive());
        
        //When Massive integer becomes small enought (the "base case"),
        //schoolbook multiplication should be called

        int thisLen = this.contents.length;
        int bLen = b.contents.length;

        int[] resultContents = new int[thisLen+bLen];
        
        //core multiplication algorithm
        for (int i = 0; i<thisLen; i++){
            long carry = 0;
            for (int j = 0; j < bLen; j++){

                long product = ((long)this.contents[i])*((long)b.contents[j]);
                long res = product + resultContents[i+j] + carry;

                resultContents[i+j] = (int)(res%BASE);
                carry = res / BASE;

            }
            //if there is leftover carry after each time that
            // all of massiveInt b is multipled by an element of contents of this massiveInt,
            // this method part adds it on
            if (carry!=0){
                int carryIdx = i+bLen;
                while (carry>0 && carryIdx < resultContents.length){
                    long sum = resultContents[carryIdx] + carry;
                    resultContents[carryIdx] = (int)(sum%BASE);
                    carry = sum / BASE;
                    carryIdx++;
                }

            }

        }
        
        MassiveInteger result = new MassiveInteger(resultContents,positiveRes);
        result.trim();
        return result;

    }

    
    public MassiveInteger scalarMultiply(int scalar){
        
        boolean positiveRes = (this.positive==(scalar>=0));
        int thisLen = this.contents.length;
        int[] resultContents = new int[thisLen+1]; 
        long carry = 0;

        //go thru and multiply each element of contents
        for (int i = 0; i<thisLen; i++){
            long res = (long)this.contents[i] * scalar + carry;
            resultContents[i]=(int)(res%BASE);
            carry = res/BASE;
        }
        if (carry!=0){ //if there is carry over at the end
            resultContents[thisLen] = (int)carry;
        }
        MassiveInteger result = new MassiveInteger(resultContents,positiveRes);
        result.trim();
        return result;
    }

     
    public MassiveInteger scalarDivide(int scalar){
        if (scalar==0){
            System.err.println("NO DIVISION BY 0 PLZ");
        }
        if (this.length()==1 && this.contents[0]==0){ //if massiveInt is just 0
            return this;
        }

        boolean positiveRes = (this.positive==(scalar>=0));
        scalar = Math.abs(scalar);
        int thisLen = this.contents.length;
        int[] resultContents = new int[thisLen];
        long remainder = 0;

        //core division algo (suprisingly simple given the other methods!)
        for (int i = thisLen-1; i>=0;i--){
            long toDivide = (remainder*BASE)+this.contents[i];
            resultContents[i]=(int)(toDivide/scalar);
            remainder = toDivide%scalar;
        }
        //due to the nature of Toom-Cook, there will never be a decimal remainder

        MassiveInteger result = new MassiveInteger(resultContents,positiveRes);
        result.trim();
        return result;

    }

    
    public MassiveInteger leftShift(int shiftFactor){
        /*
        shifts shiftfactor elements of contents[],
        not normal arithmetiic shift
         */
        if (this.length()==1 && this.contents[0]==0){
            return this;
        }
        int[] shiftedContents = new int[this.contents.length + shiftFactor];
        //since shift is always by elements of contents,
        //very easy to use built in arraycopy 
        System.arraycopy(
            this.contents,
            0,
            shiftedContents, 
            shiftFactor, 
            this.contents.length);
        return new MassiveInteger(shiftedContents, this.isPositive());

    }
    
    public String toString(){
        String result = "";
        if (this.positive == false){
            result += "-";
        }
        result += this.contents[this.contents.length - 1];
        for (int i = this.contents.length - 2; i >= 0; i--){
            result += String.format("%09d", this.contents[i]);
        }
        return result;

    }











}
