public class MOROCCOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MOROCCO__NORTH_OF_31_5_N__MERCHICH__NORD_MAROC__26191.SP":
                MyData.push("SECONDO_S_CRS", "26191");
                DataSaved.S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOROCCO__SOUTH_OF_27_9_N__MERCHICH__SAHARA__26193.SP":
                MyData.push("SECONDO_S_CRS", "26193");
                DataSaved.S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOROCCO__SOUTH_OF_31_5_N__MERCHICH__SUD_MAROC__26192.SP":
                MyData.push("SECONDO_S_CRS", "26192");
                DataSaved.S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}