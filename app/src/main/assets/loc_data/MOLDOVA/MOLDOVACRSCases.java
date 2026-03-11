public class MOLDOVACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MOLDOVA__EAST_OF_30_E__WGS_84__TMZN36N__4038.SP":
                MyData.push("SECONDO_S_CRS", "4038");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOLDOVA__MOLDREF99__4000.SP":
                MyData.push("SECONDO_S_CRS", "4000");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOLDOVA__MOLDREF99__MOLDOVA_TM__4026.SP":
                MyData.push("SECONDO_S_CRS", "4026");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOLDOVA__WEST_OF_30_E__WGS_84__TMZN35N__4037.SP":
                MyData.push("SECONDO_S_CRS", "4037");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}