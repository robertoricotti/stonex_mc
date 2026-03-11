public class LATIN_AMERICACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LATIN_AMERICA__102_W_TO_96_W__SIRGAS_2000__UTM_ZONE_14N__31968.SP":
                MyData.push("crs", "31968");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__108_W_TO_102_W__SIRGAS_2000__UTM_ZONE_13N__31967.SP":
                MyData.push("crs", "31967");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__114_W_TO_108_W__SIRGAS_2000__UTM_ZONE_12N__31966.SP":
                MyData.push("crs", "31966");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__120_W_TO_114_W__SIRGAS_2000__UTM_ZONE_11N__31965.SP":
                MyData.push("crs", "31965");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__78_W_TO_72_WEST__N_HEMISPHERE_AND_SIRGAS_BY_COUNTRY__SIRGAS_2000__UTM_ZONE_18N__31972.SP":
                MyData.push("crs", "31972");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__84_W_TO_78_WEST__N_HEMISPHERE_AND_SIRGAS_BY_COUNTRY__SIRGAS_2000__UTM_ZONE_17N__31971.SP":
                MyData.push("crs", "31971");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__90_W_TO_84_W__N_HEMISPHERE_AND_SIRGAS_2000_BY_COUNTRY__SIRGAS_2000__UTM_ZONE_16N__31970.SP":
                MyData.push("crs", "31970");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__96_W_TO_90_W__N_HEMISPHERE_AND_SIRGAS_2000_BY_COUNTRY__SIRGAS_2000__UTM_ZONE_15N__31969.SP":
                MyData.push("crs", "31969");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "LATIN_AMERICA__SIRGAS_2000_BY_COUNTRY__SIRGAS_2000__4988.SP":
                MyData.push("crs", "4988");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}