package org.h2.engine;

import org.h2.api.CustomDataTypesHandler;
import org.h2.api.ErrorCode;
import org.h2.message.DbException;
import org.h2.store.DataHandler;
import org.h2.util.JdbcUtils;
import org.h2.value.*;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

public class PasswordDataTypeHandler implements CustomDataTypesHandler {

    /** Type name for Password */
    public final static String PASSWORD_DATA_TYPE_NAME = "password";

    /** Type id for password as 10001 */
    public final static int PASSWORD_DATA_TYPE_ID = 1001;

    /** Order for password data type */
    public final static int PASSWORD_DATA_TYPE_ORDER = 100_000;

    /** Cached DataType instance for password */
    public final DataType passwordDataType;

    /** */
    public PasswordDataTypeHandler() {
        passwordDataType = createPassword();
    }

    @Override
    public DataType getDataTypeByName(String name) {
        if (name.toLowerCase(Locale.ENGLISH).equals(PASSWORD_DATA_TYPE_NAME)) {
            return passwordDataType;
        }
        return null;
    }

    @Override
    public DataType getDataTypeById(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return passwordDataType;
        }
        return null;
    }

    @Override
    public TypeInfo getTypeInfoById(int type, long precision, int scale, ExtTypeInfo extTypeInfo) {
        return new TypeInfo(type, 0, 0, ValueDouble.DISPLAY_SIZE * 2 + 1, null);
    }

    @Override
    public String getDataTypeClassName(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return Password.class.getName();
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }

    @Override
    public int getTypeIdFromClass(Class<?> cls) {
        if (cls == Password.class) {
            return PASSWORD_DATA_TYPE_ID;
        }
        return Value.JAVA_OBJECT;
    }

    @Override
    public Value convert(Value source, int targetType) {
        if (source.getValueType() == targetType) {
            return source;
        }
        if (targetType == PASSWORD_DATA_TYPE_ID) {
            switch (source.getValueType()) {
                case Value.JAVA_OBJECT: {
                    assert source instanceof ValueJavaObject;
                    return ValuePassword.get((Password)
                            JdbcUtils.deserialize(source.getBytesNoCopy(), null));
                }
                case Value.STRING: {
                    assert source instanceof  ValueString;
                    return ValuePassword.get(
                            Password.parsePassword(source.getString()));
                }
                case Value.BYTES: {
                    assert source instanceof  ValueBytes;
                    return ValuePassword.get((Password)
                            JdbcUtils.deserialize(source.getBytesNoCopy(), null));
                }
                case Value.DOUBLE: {
                    assert source instanceof  ValueDouble;
                    return ValuePassword.get((Password)
                            JdbcUtils.deserialize(source.getBytesNoCopy(), null));
                }
            }

            throw DbException.get(
                    ErrorCode.DATA_CONVERSION_ERROR_1, source.getString());
        } else {
            return source.convertTo(targetType);
        }
    }

    @Override
    public int getDataTypeOrder(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return PASSWORD_DATA_TYPE_ORDER;
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }

    @Override
    public Value getValue(int type, Object data, DataHandler dataHandler) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            assert data instanceof Password;
            return ValuePassword.get((Password)data);
        }
        return ValueJavaObject.getNoCopy(data, null, dataHandler);
    }

    @Override
    public Object getObject(Value value, Class<?> cls) {
        if (cls.equals(Password.class)) {
            if (value.getValueType() == PASSWORD_DATA_TYPE_ID) {
                return value.getObject();
            }
            return convert(value, PASSWORD_DATA_TYPE_ID).getObject();
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + value.getValueType());
    }

    @Override
    public boolean supportsAdd(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return true;
        }
        return false;
    }

    @Override
    public int getAddProofType(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return type;
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }

    /** Constructs data type instance for password type */
    private static DataType createPassword() {
        DataType result = new DataType();
        result.type = PASSWORD_DATA_TYPE_ID;
        result.name = PASSWORD_DATA_TYPE_NAME;
        result.sqlType = Types.JAVA_OBJECT;
        return result;
    }
}

/**
 * Value type implementation that holds the complex number
 */
class ValuePassword extends Value {

    private Password val;

    /**
     * @param val password
     */
    public ValuePassword(Password val) {
        assert val != null;
        this.val = val;
    }

    /**
     * Get ValuePassword instance for given Password.
     *
     * @param val password
     * @return resulting instance
     */
    public static ValuePassword get(Password val) {
        return new ValuePassword(val);
    }

    @Override
    public StringBuilder getSQL(StringBuilder builder) {
        return builder.append(val.toString());
    }

    @Override
    public TypeInfo getType() {
        return TypeInfo.getTypeInfo(PasswordDataTypeHandler.PASSWORD_DATA_TYPE_ID);
    }

    @Override
    public int getValueType() {
        return PasswordDataTypeHandler.PASSWORD_DATA_TYPE_ID;
    }

    @Override
    public String getString() {
        return val.toString();
    }

    @Override
    public Object getObject() {
        return val;
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        Object obj = JdbcUtils.deserialize(getBytesNoCopy(), getDataHandler());
        prep.setObject(parameterIndex, obj, Types.JAVA_OBJECT);
    }

    @Override
    public int compareTypeSafe(Value v, CompareMode mode) {
        return 0;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof ValuePassword)) {
            return false;
        }
        ValuePassword password = (ValuePassword)other;
        return password.val.equals(val);
    }

    @Override
    protected Value convertTo(int targetType, Mode mode, Object column, ExtTypeInfo extTypeInfo) {
        if (getValueType() == targetType) {
            return this;
        }
        switch (targetType) {
            case Value.BYTES: {
                return ValueBytes.getNoCopy(JdbcUtils.serialize(val, null));
            }
            case Value.STRING: {
                return ValueString.get(val.toString());
            }
            case Value.DOUBLE: {
                return ValueBytes.getNoCopy(JdbcUtils.serialize(val, null));
            }
            case Value.JAVA_OBJECT: {
                return ValueJavaObject.getNoCopy(JdbcUtils.serialize(val, null));
            }
        }

        throw DbException.get(
                ErrorCode.DATA_CONVERSION_ERROR_1, getString());
    }

    @Override
    public Value add(Value value) {
        ValuePassword v = (ValuePassword)value;
        return ValuePassword.get(new Password(val.pwdstr+v.val.pwdstr));
    }
}

/**
 * Password
 */
class Password implements Serializable {
    /** */

    /**
     * Password String
     */
    String pwdstr;

    /**
     * @param pwdstr Password string
     */
    public Password(String pwdstr) {
        this.pwdstr = pwdstr;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Password)) {
            return false;
        }
        Password password = (Password)other;
        return pwdstr.equals(password.pwdstr);
    }

    @Override
    public String toString() {
        return pwdstr;
    }

    /**
     * Simple parser for complex numbers. Both real and im components
     * must be written in non scientific notation.
     * @param password String.
     * @return {@link Password} object.
     */
    public static Password parsePassword(String password){

        /**
         * Check the password length
         */
        if(password.length() < 7)
        {
            throw new IllegalArgumentException();
            //throw new Exception("Password not correctly Formatted. Should have atleast more than 7 characters");
        }

        /**
         * Check for atleast one upper case character
         */
        boolean atLeastOneUpperCase = !password.equals(password.toLowerCase());


        /**
         * Throw exception if password does not contain atleast a upper case of lower case letter
         */
        if(!atLeastOneUpperCase)
        {
            throw new IllegalArgumentException();
            //throw new Exception("Password must contain 1 upper case letter");
        }

        /**
         * Check for atleast one lower case character
         */
        boolean atLeastOneLowerCase = !password.equals(password.toUpperCase());

        /**
         * Throw exception if password does not contain atleast a upper case of lower case letter
         */
        if(!atLeastOneLowerCase)
        {
            throw new IllegalArgumentException();
            //throw new Exception("Password must contain 1 lower case letter");
        }

        /**
         * Check for atleast one digit in password
         */
        boolean hasAtleastOneDigit = false;
        for(int i = 0; i < password.length(); i++)
        {
            if(Character.isDigit(password.charAt(i)))
            {
                hasAtleastOneDigit = true;
                break;
            }

        }
        /**
         * Throw exception if password does not contain atleast one digit
         */
        if(!hasAtleastOneDigit){
            throw new IllegalArgumentException();
            //throw new Exception("Password must contain atleast one digit");
        }

        return new Password(password);
    }
}
