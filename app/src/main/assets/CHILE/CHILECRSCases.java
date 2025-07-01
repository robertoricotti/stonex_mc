public class CHILECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CHILE__72_W_TO_66_W__SIRGAS_CHILE_2002__UTM_ZONE_19S__5361.SP":
                MyData.push("crs", "5361");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHILE__78_W_TO_72_W__SIRGAS_CHILE_2002__UTM_ZONE_18S__5362.SP":
                MyData.push("crs", "5362");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHILE__SIRGAS_CHILE_2002__5358.SP":
                MyData.push("crs", "5358");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}