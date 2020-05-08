package server;

public class ConfigPort {

    private static ConfigPort configPort = null;

    private ConfigPort() {

    }

    public static ConfigPort getInstance() {
        if (configPort == null) {
            configPort = new ConfigPort();
        }
        return configPort;
    }

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
