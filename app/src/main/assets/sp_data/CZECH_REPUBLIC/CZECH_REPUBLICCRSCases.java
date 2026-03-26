public class CZECH_REPUBLICCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CZECH_REPUBLIC__S_JTSK__KROVAK_EAST_NORTH__5514.SP":
                MyData.push("crs", "5514");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
                case "CZECH_REPUBLIC__S_JTSK__KROVAK_POSITIVE_5513.SP":
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
                case "CZECH_REPUBLIC__S_JTSK__KROVAK_MODIFIED__5516.SP":
                MyData.push("crs", "5516");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;        
           
        
            case "SJTSK_FERRO_NO_V1710_Krovak_Modified.SP":
                MyData.push("crs", "150581");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SJTSK_FERRO_SO_V1710_Krovak_Modified.SP":
                MyData.push("crs", "150582");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SJTSK_FERRO_UNI.SP":
                MyData.push("crs", "150583");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}