public class ST_PIERRE_AND_MIQUELONCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ST_PIERRE_AND_MIQUELON__ONSHORE__SAINT_PIERRE_ET_MIQUELON_1950__UTM_ZONE_21N__2987.SP":
                MyData.push("SECONDO_S_CRS", "2987");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ST_PIERRE_AND_MIQUELON__RGSPM06__4465.SP":
                MyData.push("SECONDO_S_CRS", "4465");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ST_PIERRE_AND_MIQUELON__RGSPM06__UTM_ZONE_21N__4467.SP":
                MyData.push("SECONDO_S_CRS", "4467");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}