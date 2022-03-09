package xyz.rodit.dexsearch.client;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ClassMapping {

    @SerializedName("name")
    private final String niceClassName;
    @SerializedName("dex_name")
    private final String dexClassName;
    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, String> methods = new HashMap<>();

    public ClassMapping(String niceClassName, String dexClassName) {
        this.niceClassName = niceClassName;
        this.dexClassName = dexClassName;
    }

    public String getNiceClassName() {
        return niceClassName;
    }

    public String getDexClassName() {
        return dexClassName;
    }

    public String getDexField(String niceName) {
        return fields.get(niceName);
    }

    public void mapField(String niceName, String dexName) {
        fields.put(niceName, dexName);
    }

    public String getDexMethod(String niceName) {
        return methods.get(niceName);
    }

    public void mapMethod(String niceName, String dexName) {
        methods.put(niceName, dexName);
    }
}
