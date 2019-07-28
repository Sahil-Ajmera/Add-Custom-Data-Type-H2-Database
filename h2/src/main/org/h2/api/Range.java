package org.h2.api;


/**
 *  AggregateFunction implementing class
 */
public class Range implements AggregateFunction {

    // Keep count of the number of records
    private int count = 0;

    // Keep track of the result object which in our case is the range of the column
    private Integer result;

    // Keep track of the max value found in the column
    private Object max;

    // Keep track of the min value found in the column
    private Object min;

    /**
     * This method is called when the aggregate function is used.
     * A new object is created for each invocation.
     *
     * @param cnctn a connection to the database
     */
    @Override
    public void init(java.sql.Connection cnctn) throws java.sql.SQLException {
        // Initialization of the object
    }

    /**
     * This method must return the SQL type of the method, given the SQL type of
     * the input data. The method should check here if the number of parameters
     * passed is correct, and if not it should throw an exception.
     *
     * @param ints the SQL type of the parameters, {@link java.sql.Types}
     * @return the SQL type of the result
     * */
    @Override
    public int getType(int[] ints) throws java.sql.SQLException {
        return ints[0];
    }

    /**
     * This method is called once for each row.
     * If the aggregate function is called with multiple parameters,
     * those are passed as array.
     *
     * @param o the value(s) for this row
     */
    @Override
    public void add(Object o) throws java.sql.SQLException {
        Object value = o;

      // Add first object encountered in the list of records
        if (count == 0) {
            max = o;
            min = o;
        }
        // For every other object
        else
            {
                // Check if the object is a string object
            if(o instanceof String){
                // Do string related comparison for max
                if(CompareTwoStrings((String)o, (String)max) > 0){
                    max = o;
                }
                // and min
                if(CompareTwoStrings((String)o, (String)min) < 0){
                    min = o;
                }
                // Store result
                result = CompareTwoStrings((String)max,(String)min);
            }
            else {
                // If the object is integer object, do integer related comparison
                if ((Integer)value < (Integer)min) {
                    min = value;
                    // Set the max and min values
                } else if ((Integer)value > (Integer)max) {
                    max = value;
                }
                // Save result
                result = (Integer)max - (Integer)min;
            }

        }
        count++;
    }

    /**
     * This method returns the computed aggregate value. This method must
     * preserve previously added values and must be able to reevaluate result if
     * more values were added since its previous invocation.
     *
     * @return the aggregated value
     */
    @Override
    public Object getResult() throws java.sql.SQLException {
        return result;
    }

    /**
     * Compares Two strings and returns an integer value denoting the difference between the two strings
     * @param str1 First string
     * @param str2 Second String
     * @return int value denoting difference between the strings
     */
    public static int CompareTwoStrings(String str1, String str2)
    {
        // Initialization
        int l1 = str1.length();
        int l2 = str2.length();
        int lengthmin = Math.min(l1, l2);

        // Compare character by character and compare the two strings
        for (int i = 0; i < lengthmin; i++) {
            int ch1 = (int)str1.charAt(i);
            int ch2 = (int)str2.charAt(i);

            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }

        if (l1 != l2) {
            return l1 - l2;
        }
        else {
            return 0;
        }
    }
}
