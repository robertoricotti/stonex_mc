public class PAPUA_NEW_GUINEACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PAPUA_NEW_GUINEA__144_E_TO_150_E__PNG94__PNGMG94_ZONE_55__5551.SP":
                MyData.push("SECONDO_S_CRS", "5551");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__150_E_TO_156_E__PNG94__PNGMG94_ZONE_56__5552.SP":
                MyData.push("SECONDO_S_CRS", "5552");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__PNG94__5544.SP":
                MyData.push("SECONDO_S_CRS", "5544");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__WEST_OF_144_E__PNG94__PNGMG94_ZONE_54__5550.SP":
                MyData.push("SECONDO_S_CRS", "5550");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}