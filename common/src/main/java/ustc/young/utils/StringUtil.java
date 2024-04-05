package ustc.young.utils;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-28 13:45
 **/
public class StringUtil {
    public static boolean isBlank(String s){
        if(s==null||s.length()==0){
            return true;
        }
        for(int i=0;i<s.length();i++){
            if(!Character.isWhitespace(s.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
