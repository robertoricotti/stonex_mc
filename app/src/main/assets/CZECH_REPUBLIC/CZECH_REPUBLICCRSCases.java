public class CZECH_REPUBLICCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CZECH_REPUBLIC__S_JTSK__KROVAK_EAST_NORTH__5514.SP":
                MyData.push("crs", "27700");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CZECH_REPUBLIC__S_JTSK__KROVAK__5513.SP":
                MyData.push("crs", "5513");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CZECH_REPUBLIC__S_JTSK__KROVAK_EAST_NORTH__FERRO__5221.SP":
                MyData.push("crs", "5221");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CZECH_REPUBLIC__ETRS89__UTM_ZONE_33N__25833.SP":
                MyData.push("crs", "25833");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CZECH_REPUBLIC__ETRS89__UTM_ZONE_34N__25834.SP":
                MyData.push("crs", "25834");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}