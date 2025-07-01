public class CAPE_VERDECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CAPE_VERDE__WGS_84__CAPE_VERDE_NATIONAL__4826.SP":
                MyData.push("crs", "4826");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}