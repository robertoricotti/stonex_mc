public class CAPE_VERDECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CAPE_VERDE__WGS_84__CAPE_VERDE_NATIONAL__4826.SP":
                MyData.push("SECONDO_S_CRS", "4826");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}