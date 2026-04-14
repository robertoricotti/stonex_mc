public class BELGIUMCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BELGIUM__ONSHORE__BD50__BRUSSELS__BELGE_LAMBERT_50__21500.SP":
                MyData.push("SECONDO_S_CRS", "21500");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BELGIUM__ONSHORE__BD72__BELGE_LAMBERT_72__31300.SP":
                MyData.push("SECONDO_S_CRS", "31300");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BELGIUM__ONSHORE__BD72__BELGIAN_LAMBERT_72__31370.SP":
                MyData.push("SECONDO_S_CRS", "31370");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BELGIUM__ONSHORE__ETRS89__BELGIAN_LAMBERT_2005__3447.SP":
                MyData.push("SECONDO_S_CRS", "3447");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BELGIUM__ONSHORE__ETRS89__BELGIAN_LAMBERT_2008__3812.SP":
                MyData.push("SECONDO_S_CRS", "3812");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}