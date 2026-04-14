public class MONTSERRATCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MONTSERRAT__ONSHORE__MONTSERRAT_1958__BRITISH_WEST_INDIES_GRID__2004.SP":
                MyData.push("crs", "2004");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}