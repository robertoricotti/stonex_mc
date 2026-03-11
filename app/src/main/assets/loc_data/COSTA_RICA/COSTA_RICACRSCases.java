public class COSTA_RICACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "COSTA_RICA__CR05__5363.SP":
                MyData.push("SECONDO_S_CRS", "5363");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COSTA_RICA__ONSHORE_AND_OFFSHORE_EAST_OF_86_30_W__CR05__CRTM05__5367.SP":
                MyData.push("SECONDO_S_CRS", "5367");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COSTA_RICA__ONSHORE_NORTH_OF_9_32_N__OCOTEPEQUE_1935__COSTA_RICA_NORTE__5456.SP":
                MyData.push("SECONDO_S_CRS", "5456");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "COSTA_RICA__ONSHORE_SOUTH_OF_9_56_N__OCOTEPEQUE_1935__COSTA_RICA_SUR__5457.SP":
                MyData.push("SECONDO_S_CRS", "5457");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}