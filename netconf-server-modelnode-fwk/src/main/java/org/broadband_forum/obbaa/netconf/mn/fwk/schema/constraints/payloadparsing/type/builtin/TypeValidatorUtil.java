package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import com.google.common.collect.Range;

public class TypeValidatorUtil {
	
	public static final Map<String, String> PATTERNNAMEMAPS  = new HashMap<String, String>();
	private static final String IPV4PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?$";
	private static final String IPV6_1_PATTERN = "^((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?$";
	private static final String IPV6_2_PATTERN = "^(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?$";
	private static final String URL_PATTERN = "^((http|https|tftp)://|(ftp|sftp)://((\\S+:\\S+)@))([\\S&&[^/]]+)/([\\S&&[^:]]+)$";
	private static final String UTONLY = "^abc$";
	
	private static final Long MAX_UINT8 = 255L;
	private static final Long MAX_UINT16 = 65535L;
	private static final Long MAX_UINT32 = 4294967295L;
	private static final BigInteger MAX_UINT64 = new BigInteger("18446744073709551615");
	
	   static {
		   PATTERNNAMEMAPS.put(IPV4PATTERN, "[0-255]. [0-255]. [0-255]. [0-255]");
		   PATTERNNAMEMAPS.put(IPV6_1_PATTERN, "xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx");
		   PATTERNNAMEMAPS.put(IPV6_2_PATTERN, "::x (x would be a hexadecimal value)");
		   PATTERNNAMEMAPS.put(UTONLY, "must be abc");
		   PATTERNNAMEMAPS.put(URL_PATTERN, "[ftp|sftp]://[user]:[password]@[host]/[location] or [http|https|tftp]://[host]/[location]");
		   
	    }
	
	public static byte getInt8Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return Byte.parseByte(value);
		} catch (NumberFormatException e) {		
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}

	public static short getInt16Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return Short.parseShort(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}

	public static int getInt32Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}

	public static long getInt64Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static BigInteger getUint64Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			BigInteger bigIntegerValue = new BigInteger(value);
			if (bigIntegerValue.compareTo(BigInteger.ZERO) < 0 || bigIntegerValue.compareTo(MAX_UINT64) > 0) {
				throw getOutOfRangeException(value, rangeConstraint);
			}
			return bigIntegerValue;
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static short getUint8Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			short shortValue = Short.parseShort(value);
			if (shortValue < 0 || shortValue > MAX_UINT8) {
				throw getOutOfRangeException(value, rangeConstraint);
			}
			return shortValue;
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static int getUint16Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			int intValue = Integer.parseInt(value);
			if (intValue < 0 || intValue > MAX_UINT16) {
				throw getOutOfRangeException(value, rangeConstraint);
			}
			return intValue;
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static long getUint32Value(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			long longValue = Long.parseLong(value);
			if (longValue < 0 || longValue > MAX_UINT32) {
				throw getOutOfRangeException(value, rangeConstraint);
			}
			return longValue;
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static double getDoubleValue(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static ValidationException getOutOfRangeException(String value, String min, String max) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, 
				 "Value \"" + value + "\" does not meet the range constraints. Expected range of value: " + min + ".." + max);
		return new ValidationException(rpcError);
	}

	public static String getRangeString(RangeConstraint<?> rangeConstraint) {
		StringBuilder result = new StringBuilder();
		for (Range<?> range : rangeConstraint.getAllowedRanges().asRanges()) {
			result.append(range.lowerEndpoint());
			result.append("..");
			result.append(range.upperEndpoint());
			result.append(" | ");
		}

		return result.substring(0, result.length() - 3);
	}
	
	public static String getLengthString(LengthConstraint lengthConstraint) {
		StringBuilder result = new StringBuilder();
		for (Range<Integer> length : lengthConstraint.getAllowedRanges().asRanges()) {
			result.append(length.lowerEndpoint());
			if (!length.lowerEndpoint().equals(length.upperEndpoint())) {
				result.append("..");
				result.append(length.upperEndpoint());
			}
			
			result.append(" | ");
		}
		
		return result.substring(0, result.length() - 3);
	}
	
	public static String getPatternStrings(List<Pattern> patternList) {
		StringBuilder result = new StringBuilder();
		for (Pattern pattern : patternList) {
			String patternString = pattern.toString();
			String friendlyName = PATTERNNAMEMAPS.get(patternString);
			if (friendlyName != null){
				result.append(friendlyName);
			} else {
				result.append(pattern.toString());
			}
			result.append(" or ");
		}
		
		return result.substring(0, result.length() - 4);
	}

	public static ValidationException getPatternConstraintException(String value, List<PatternConstraint> patternConstraints) {
		
		StringBuilder fullErrorApptag = new StringBuilder();
		StringBuilder fullErrorMsg = new StringBuilder();

		for(PatternConstraint constraint : patternConstraints){
		    if (constraint.getErrorAppTag().isPresent()) {
		        fullErrorApptag.append(constraint.getErrorAppTag().get());
		    }
		    if (constraint.getErrorMessage().isPresent()) {
		        fullErrorMsg.append(constraint.getErrorMessage().get());
		    }
		    else {
		        // for backward compatibility with older ODL releases
		        fullErrorMsg.append("Supplied value does not match the regular expression ").append(constraint.getJavaPatternString()).append(".");
		    }
			fullErrorApptag.append(" or ");
			fullErrorMsg.append(" or ");
		}
		
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, fullErrorMsg.substring(0, fullErrorMsg.length() - 4));
		rpcError.setErrorAppTag(fullErrorApptag.substring(0, fullErrorApptag.length() - 4));	
		return new ValidationException(rpcError);
	}
	
	public static ValidationException getLengthConstraintException(String value, LengthConstraint lengthConstraint) {
	    String errorAppTag = "";
	    if (lengthConstraint.getErrorAppTag().isPresent()) {
	        errorAppTag = lengthConstraint.getErrorAppTag().get();
	    }
	    else {
	        // for backward compatibility with older ODL releases
	        errorAppTag = "length-out-of-specified-bounds";
	    }
	    
	    String errorMsg = "";
	    if (lengthConstraint.getErrorMessage().isPresent()) {
	        errorMsg = lengthConstraint.getErrorMessage().get();
	    }
        else {
            // for backward compatibility with older ODL releases
            StringBuilder sb = new StringBuilder();
            sb.append("The argument is out of bounds ");
            for (Range<?> range : lengthConstraint.getAllowedRanges().asRanges()) {
                sb.append("<").append(range.lowerEndpoint()).append(", ").append(range.upperEndpoint()).append(">");
                sb.append(", ");
            }
            errorMsg = sb.toString();
            errorMsg = errorMsg.substring(0, errorMsg.length() - 2);
        }
	
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, errorMsg);
		rpcError.setErrorAppTag(errorAppTag);
		
		return new ValidationException(rpcError);
	}
	
	public static ValidationException getOutOfRangeException(String value, RangeConstraint<?> rangeConstraint) {
		String errorAppTag = "";
		if (rangeConstraint.getErrorAppTag().isPresent()) {
		    errorAppTag = rangeConstraint.getErrorAppTag().get();
		}
		else {
		    // for backward compatibility with older ODL releases
		    errorAppTag = "range-out-of-specified-bounds";
		}
		
		String errorMsg = "";
		if (rangeConstraint.getErrorMessage().isPresent()) {
		    errorMsg = rangeConstraint.getErrorMessage().get();
		}
		else {
		    // for backward compatibility with older ODL releases
		    StringBuilder sb = new StringBuilder();
		    sb.append("The argument is out of bounds ");
		    for (Range<?> range : rangeConstraint.getAllowedRanges().asRanges()) {
                sb.append("<").append(range.lowerEndpoint()).append(", ").append(range.upperEndpoint()).append(">");
                sb.append(", ");
            }
		    errorMsg = sb.toString();
            errorMsg = errorMsg.substring(0, errorMsg.length() - 2);
		}
		
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, errorMsg);
		rpcError.setErrorAppTag(errorAppTag);
		return new ValidationException(rpcError);
	}
	
	public static ValidationException getEmptyTypeException(String value, String elementName) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
				String.format(
						"Value \"%s\" does not meet the empty type constraints. Element \"%s\" should not have any value", value,
						elementName));
		return new ValidationException(rpcError);
	}

	public static ValidationException getInvalidFormatBitsException(String textValue) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(
				NetconfRpcErrorTag.INVALID_VALUE,
				String.format(
						"Value \"%s\" does not meet the bits type constraints. It shouldn't have any spaces in begin/end value and more than one space between bit values",
						textValue));
		return new ValidationException(rpcError);
	}

	public static ValidationException getInvalidBitsException(List<String> invalidBitValues, List<String> bitNames) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, String.format(
						"Value \"%s\" does not meet the bits type constraints. Valid bits are: \"%s\"",
						formatListToString(invalidBitValues), formatListToString(bitNames)));
		return new ValidationException(rpcError);
	}

	public static ValidationException getDuplicatedBitsException(String value) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, 
				 String.format(
						"Value \"%s\" does not meet the bits type constraints. A bit value can appear atmost once", value));
		return new ValidationException(rpcError);
	}
	
	public static BigDecimal getBigDecimalValue(String value, RangeConstraint<?> rangeConstraint) throws ValidationException {
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, rangeConstraint);
		}
	}
	
	public static BigDecimal getBigDecimalValue(String value, BigDecimal min, BigDecimal max) throws ValidationException {
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw getOutOfRangeException(value, min.toString(), max.toString());
		}
	}
	
	public static String formatListToString(List<String> strs) {
		StringBuilder strBuilder = new StringBuilder();
		for (String str : strs) {
			strBuilder.append(str).append(", ");
		}
		if (strBuilder.length() > 0) {
			strBuilder.deleteCharAt(strBuilder.length() - 1);
			strBuilder.deleteCharAt(strBuilder.length() - 1);
		}
		return strBuilder.toString();
	}
}
