public class ST_PIERRE_AND_MIQUELONCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_PIERRE_AND_MIQUELON__ONSHORE__SAINT_PIERRE_ET_MIQUELON_1950__UTM_ZONE_21N__2987.SP":
                MyData.push("crs", "2987");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ST_PIERRE_AND_MIQUELON__RGSPM06__4465.SP":
                MyData.push("crs", "4465");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ST_PIERRE_AND_MIQUELON__RGSPM06__UTM_ZONE_21N__4467.SP":
                MyData.push("crs", "4467");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}