public class SPAINCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SPAIN__CANARY_ISLANDS__EAST_OF_18_W__REGCAN95__UTM_ZONE_28N__4083.SP":
                MyData.push("SECONDO_S_CRS", "4083");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SPAIN__CANARY_ISLANDS__REGCAN95__4079.SP":
                MyData.push("SECONDO_S_CRS", "4079");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SPAIN__CANARY_ISLANDS__REGCAN95__LAEA_EUROPE__5635.SP":
                MyData.push("SECONDO_S_CRS", "5635");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SPAIN__CANARY_ISLANDS__REGCAN95__LCC_EUROPE__5634.SP":
                MyData.push("SECONDO_S_CRS", "5634");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SPAIN__CANARY_ISLANDS__WEST_OF_18_W__REGCAN95__UTM_ZONE_27N__4082.SP":
                MyData.push("SECONDO_S_CRS", "4082");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SPAIN__MAINLAND_ONSHORE__MADRID_1870__MADRID__SPAIN_LCC__2062.SP":
                MyData.push("SECONDO_S_CRS", "2062");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}