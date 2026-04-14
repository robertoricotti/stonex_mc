public class CHILECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CHILE__72_W_TO_66_W__SIRGAS_CHILE_2002__UTM_ZONE_19S__5361.SP":
                MyData.push("SECONDO_S_CRS", "5361");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHILE__78_W_TO_72_W__SIRGAS_CHILE_2002__UTM_ZONE_18S__5362.SP":
                MyData.push("SECONDO_S_CRS", "5362");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHILE__SIRGAS_CHILE_2002__5358.SP":
                MyData.push("SECONDO_S_CRS", "5358");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}