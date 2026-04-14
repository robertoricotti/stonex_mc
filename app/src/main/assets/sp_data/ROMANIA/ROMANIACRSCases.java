public class ROMANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ROMANIA__DEALUL_PISCULUI_1970__STEREO_70__31700.SP":
                MyData.push("crs", "31700");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__OFFSHORE__ETRS89__TM_30_NE__2213.SP":
                MyData.push("crs", "2213");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__ONSHORE__DEALUL_PISCULUI_1930__STEREO_33__31600.SP":
                MyData.push("crs", "31600");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ROMANIA__PULKOVO_1942_58__STEREO70__3844.SP":
                MyData.push("crs", "3844");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}