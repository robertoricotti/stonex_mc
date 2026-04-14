public class CROATIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CROATIA__EAST_OF_18_E__HTRS96__UTM_ZONE_34N__3768.SP":
                MyData.push("crs", "3768");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__HTRS96__4888.SP":
                MyData.push("crs", "4888");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__HTRS96__CROATIA_LCC__3766.SP":
                MyData.push("crs", "3766");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__ONSHORE__HTRS96__CROATIA_TM__3765.SP":
                MyData.push("crs", "3765");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CROATIA__WEST_OF_18_E__HTRS96__UTM_ZONE_33N__3767.SP":
                MyData.push("crs", "3767");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}