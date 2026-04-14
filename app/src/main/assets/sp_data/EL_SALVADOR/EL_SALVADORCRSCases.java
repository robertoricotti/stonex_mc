public class EL_SALVADORCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "EL_SALVADOR__ONSHORE__OCOTEPEQUE_1935__EL_SALVADOR_LAMBERT__5460.SP":
                MyData.push("crs", "5460");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "EL_SALVADOR__SIRGAS_ES2007_8__5391.SP":
                MyData.push("crs", "5391");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}