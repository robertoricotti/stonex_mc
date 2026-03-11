public class CZECH_REPUBLICCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CZECH_REPUBLIC__S_JTSK__KROVAK_EAST_NORTH__5514.SP":
                MyData.push("SECONDO_S_CRS", "5514");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CZECH_REPUBLIC__S_JTSK__KROVAK_POSITIVE_5513.SP":
                MyData.push("SECONDO_S_CRS", "5513");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CZECH_REPUBLIC__S_JTSK__KROVAK_EAST_NORTH__FERRO__5221.SP":
                MyData.push("SECONDO_S_CRS", "5221");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "SJTSK_FERRO_NO_V1710_Grid.SP":
                MyData.push("SECONDO_S_CRS", "150581");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "SJTSK_FERRO_SO_V1710_Grid.SP":
                MyData.push("SECONDO_S_CRS", "150582");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "SJTSK_FERRO_UNI.SP":
                MyData.push("SECONDO_S_CRS", "150583");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}