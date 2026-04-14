public class MONTSERRATCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MONTSERRAT__ONSHORE__MONTSERRAT_1958__BRITISH_WEST_INDIES_GRID__2004.SP":
                MyData.push("SECONDO_S_CRS", "2004");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}