public class ROMANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ROMANIA__DEALUL_PISCULUI_1970__STEREO_70__31700.SP":
                MyData.push("SECONDO_S_CRS", "31700");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__OFFSHORE__ETRS89__TM_30_NE__2213.SP":
                MyData.push("SECONDO_S_CRS", "2213");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__ONSHORE__DEALUL_PISCULUI_1930__STEREO_33__31600.SP":
                MyData.push("SECONDO_S_CRS", "31600");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__PULKOVO_1942_58__STEREO70__3844.SP":
                MyData.push("SECONDO_S_CRS", "3844");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}