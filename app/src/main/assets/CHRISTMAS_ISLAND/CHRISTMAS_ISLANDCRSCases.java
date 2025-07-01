public class CHRISTMAS_ISLANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CHRISTMAS_ISLAND__ONSHORE__GDA94__CIG94__6721.SP":
                MyData.push("crs", "6721");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CHRISTMAS_ISLAND__ONSHORE__WGS_84__CIG92__6720.SP":
                MyData.push("crs", "6720");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}