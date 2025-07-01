public class ITALYCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ITALY__12_E_TO_18_E__IGM95__UTM_ZONE_33N__3065.SP":
                MyData.push("crs", "3065");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__12_E_TO_18_E__RDN2008__UTM_ZONE_33N__N_E__6708.SP":
                MyData.push("crs", "6708");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__EAST_OF_12_E__MONTE_MARIO__ITALY_ZONE_2__3004.SP":
                MyData.push("crs", "3004");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__EAST_OF_12_E__MONTE_MARIO__ROME__ITALY_ZONE_2__26592.SP":
                MyData.push("crs", "26592");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__EAST_OF_18_E__RDN2008__UTM_ZONE_34N__N_E__6709.SP":
                MyData.push("crs", "6709");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__EMILIA_ROMAGNA__MONTE_MARIO__TM_EMILIA_ROMAGNA__5659.SP":
                MyData.push("crs", "5659");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__INCLUDING_SAN_MARINO_AND_VATICAN__IGM95__4982.SP":
                MyData.push("crs", "4982");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__INCLUDING_SAN_MARINO_AND_VATICAN__RDN2008__6704.SP":
                MyData.push("crs", "6704");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__RDN2008__ITALY_ZONE__N_E__6875.SP":
                MyData.push("crs", "6875");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__RDN2008__ZONE_12__N_E__6876.SP":
                MyData.push("crs", "6876");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__WEST_OF_12_E__IGM95__UTM_ZONE_32N__3064.SP":
                MyData.push("crs", "3064");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__WEST_OF_12_E__MONTE_MARIO__ITALY_ZONE_1__3003.SP":
                MyData.push("crs", "3003");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__WEST_OF_12_E__MONTE_MARIO__ROME__ITALY_ZONE_1__26591.SP":
                MyData.push("crs", "26591");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ITALY__WEST_OF_12_E__RDN2008__UTM_ZONE_32N__N_E__6707.SP":
                MyData.push("crs", "6707");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}