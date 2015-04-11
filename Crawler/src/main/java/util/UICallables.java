package util;

/**
 * Used as interface for the application to communicate with the User Interface.
 *
 * Created by KrisMinkjan on 11-4-2015.
 */
public interface UICallables {

    void updateInfo(int module, long ms);

    void updateConnectionStatus(String status);
}
