public class UKCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "UK__BRITAIN_AND_UKCS_49_45_N_TO_61_N__9_W_TO_2_E__OSGB36__BRITISH_NATIONAL_GRID__27700.SP":
                MyData.push("crs", "27700");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "UK__NORTHERN_IRELAND__ONSHORE__OSNI_1952__IRISH_NATIONAL_GRID__29901.SP":
                MyData.push("crs", "29901");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "UK__OFFSHORE__NORTH_SEA__ED50__TM_0_N__23090.SP":
                MyData.push("crs", "23090");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}