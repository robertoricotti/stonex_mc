package utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.util.List;

import gui.MyApp;

public class WifiHelper {
    Context context;

    public static String getConnectedSSID(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Controlla se il Wi-Fi è abilitato
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            // Controlla se il dispositivo è connesso a una rete Wi-Fi
            if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
                return wifiInfo.getSSID(); // Restituisce l'SSID della rete
            }
        }
        return null; // Non connesso a una rete Wi-Fi
    }

    public static void connectToWifi(Context context, String ssid, String password, boolean disableAutoReconnect) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Disconnetti prima di tentare una nuova connessione
        disconnectCurrentWifi(wifiManager);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Per Android 10+ (Network Suggestions)
            connectUsingNetworkSuggestions(wifiManager, ssid, password, disableAutoReconnect);
        } else {
            // Per Android 9 o versioni precedenti
            connectUsingLegacyApi(wifiManager, ssid, password, disableAutoReconnect);
        }
    }

    private static void connectUsingNetworkSuggestions(WifiManager wifiManager, String ssid, String password, boolean disableAutoReconnect) {
        WifiNetworkSuggestion.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder = new WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password);
        }

        if (disableAutoReconnect) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setIsAppInteractionRequired(true); // Impedisce la riconnessione automatica
            }
        }

        WifiNetworkSuggestion suggestion = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            suggestion = builder.build();
        }

        List<WifiNetworkSuggestion> suggestions = List.of(suggestion);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiManager.removeNetworkSuggestions(suggestions); // Rimuovi eventuali suggerimenti precedenti
        }
        int status = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            status = wifiManager.addNetworkSuggestions(suggestions);
        }

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            System.out.println("Suggerimento Wi-Fi aggiunto con successo.");
        } else {
            System.out.println("Errore nell'aggiunta del suggerimento Wi-Fi.");
        }
    }

    private static void connectUsingLegacyApi(WifiManager wifiManager, String ssid, String password, boolean disableAutoReconnect) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + password + "\"";

        if (disableAutoReconnect) {
            wifiConfig.priority = 0; // Imposta la priorità più bassa
        }

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);

        if (disableAutoReconnect) {
            // Disabilita tutte le altre reti per evitare la riconnessione automatica
            if (ActivityCompat.checkSelfPermission(MyApp.visibleActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                for (WifiConfiguration config : configuredNetworks) {
                    if (config.networkId != netId) {
                        wifiManager.disableNetwork(config.networkId);
                    }
                }
            }
        }

        wifiManager.reconnect();
    }

    public static void disconnectCurrentWifi(WifiManager wifiManager) {
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            wifiManager.disconnect(); // Disconnetti dalla rete attuale
            System.out.println("Disconnesso dalla rete attuale.");
        }
    }
}
