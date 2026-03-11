public class PERUCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PERU__78_W_TO_72_W__PERU96__UTM_ZONE_18S__5387.SP":
                MyData.push("SECONDO_S_CRS", "5387");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__79_W_TO_73_W__PSAD56__PERU_CENTRAL_ZONE__24892.SP":
                MyData.push("SECONDO_S_CRS", "24892");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__84_W_TO_78_W__PERU96__UTM_ZONE_17S__5388.SP":
                MyData.push("SECONDO_S_CRS", "5388");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__84_W_TO_78_W__PERU96__UTM_ZONE_17S__5839.SP":
                MyData.push("SECONDO_S_CRS", "5839");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__EAST_OF_72_W__PERU96__UTM_ZONE_19S__5389.SP":
                MyData.push("SECONDO_S_CRS", "5389");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__EAST_OF_73_W__PSAD56__PERU_EAST_ZONE__24893.SP":
                MyData.push("SECONDO_S_CRS", "24893");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__PERU96__5369.SP":
                MyData.push("SECONDO_S_CRS", "5369");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PERU__WEST_OF_79_W__PSAD56__PERU_WEST_ZONE__24891.SP":
                MyData.push("SECONDO_S_CRS", "24891");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}