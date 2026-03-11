public class PAPUA_NEW_GUINEACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PAPUA_NEW_GUINEA__144_E_TO_150_E__PNG94__PNGMG94_ZONE_55__5551.SP":
                MyData.push("crs", "5551");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__150_E_TO_156_E__PNG94__PNGMG94_ZONE_56__5552.SP":
                MyData.push("crs", "5552");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__PNG94__5544.SP":
                MyData.push("crs", "5544");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAPUA_NEW_GUINEA__WEST_OF_144_E__PNG94__PNGMG94_ZONE_54__5550.SP":
                MyData.push("crs", "5550");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}