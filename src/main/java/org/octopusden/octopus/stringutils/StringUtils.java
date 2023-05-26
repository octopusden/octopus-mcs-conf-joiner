package org.octopusden.octopus.stringutils;

public class StringUtils {

  public static String fillChar(char c, int count) {
    StringBuffer buffer = new StringBuffer(count);
    for (int i = 0; i < count; i++) {
      buffer.append(c);
    }
    return ( buffer.toString() );
  }

  /**
   * Parse command line params ( <paramName>=<ParamValue>)
   *
   * @param paramName
   * @return param value from command line or default value
   */
  public static String getParamFromCommandLine(String[] args, String paramName, String defaultValue) {
    String pattern = paramName + "=";
    for (String arg : args) {
      if (arg.startsWith(pattern))
        return ( arg.length() > pattern.length() ? arg.substring(pattern.length()) : "" );
    }
    return ( defaultValue );
  }

  public static int getParamIntFromCommandLine(String[] args, String paramName, int defaultValue) {
    int res = defaultValue;
    String value = getParamFromCommandLine(args, paramName, null);
    if (value != null) {
      //noinspection EmptyCatchBlock
      try {
        res = Integer.parseInt(value);
      } catch (NumberFormatException e) {
      }
    }
    return ( res );
  }

  public static final boolean isEmptyOrNull(String s) {
    return s == null || s.trim().length() == 0;
  }

  public static String appendZeros(String s, int len) {
    StringBuilder res = new StringBuilder(s);
    for (int i = 0; i < len - s.length(); i++)
      res.insert(0, '0');
    return ( res.toString() );
  }

  public static String trimRigth(String s) {
    int pos = s.length() - 1;
    while (( pos >= 0 ) && ( s.charAt(pos) == ' ' )) {
      pos--;
    }

    if (pos >= 0) return ( s.substring(0, pos) );
    else
      return ( s );
  }

  /**
   * Works correctly with non printable characters
   *
   * @param s
   * @return
   */
  public static String trim(String s) {
    int pos2 = s.length() - 1;
    while (( pos2 >= 0 ) && ( s.charAt(pos2) == ' ' )) {
      pos2--;
    }

    int pos1 = 0;
    while (pos1 < s.length() && s.charAt(pos1) == ' ') {
      pos1++;
    }


    // Empty String
    if (( pos1 >= s.length() ) && ( pos2 < 0 ))
      return ( "" );
      // Do Trimming
    else if (( pos1 < s.length() ) || ( pos2 >= 0 ))
      return ( s.substring(pos1, pos2 + 1) );
      // Not Trimming
    else
      return ( s );
  }


}
