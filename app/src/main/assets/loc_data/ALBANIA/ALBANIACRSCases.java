public class ALBANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ALBANIA__ALBANIAN_1987__GAUSS_KRUGER_ZONE_4__2199.SP":
                MyData.push("SECONDO_S_CRS", "2199");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ALBANIA__ONSHORE__ALBANIAN_1987__GAUSS_KRUGER_ZONE_4__2462.SP":
                MyData.push("SECONDO_S_CRS", "2462");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ALBANIA__ONSHORE__ETRS89__ALBANIA_LCC_2010__6962.SP":
                MyData.push("SECONDO_S_CRS", "6962");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "ALBANIA__ONSHORE__ETRS89__ALBANIA_TM_2010__6870.SP":
                MyData.push("SECONDO_S_CRS", "6870");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}