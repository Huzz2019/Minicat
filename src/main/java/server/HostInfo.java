package server;

public class HostInfo {

    private String name;

    private String appBase;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppBase() {
        return appBase;
    }

    public void setAppBase(String appBase) {
        this.appBase = appBase;
    }

    public HostInfo(String name, String appBase) {
        this.name = name;
        this.appBase = appBase;
    }
}
