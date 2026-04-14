public class CHRISTMAS_ISLANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CHRISTMAS_ISLAND__ONSHORE__GDA94__CIG94__6721.SP":
                MyData.push("SECONDO_S_CRS", "6721");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHRISTMAS_ISLAND__ONSHORE__WGS_84__CIG92__6720.SP":
                MyData.push("SECONDO_S_CRS", "6720");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}