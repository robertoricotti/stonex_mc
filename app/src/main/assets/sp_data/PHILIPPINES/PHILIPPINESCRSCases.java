public class PHILIPPINESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PHILIPPINES__PRS92__4994.SP":
                MyData.push("crs", "4994");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_III_ONSHORE__LUZON_1911__PHILIPPINES_ZONE_III__25393.SP":
                MyData.push("crs", "25393");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_III__PRS92__PHILIPPINES_ZONE_3__3123.SP":
                MyData.push("crs", "3123");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_II_ONSHORE__LUZON_1911__PHILIPPINES_ZONE_II__25392.SP":
                MyData.push("crs", "25392");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_II__PRS92__PHILIPPINES_ZONE_2__3122.SP":
                MyData.push("crs", "3122");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_IV_ONSHORE__LUZON_1911__PHILIPPINES_ZONE_IV__25394.SP":
                MyData.push("crs", "25394");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_IV__PRS92__PHILIPPINES_ZONE_4__3124.SP":
                MyData.push("crs", "3124");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_I_ONSHORE__LUZON_1911__PHILIPPINES_ZONE_I__25391.SP":
                MyData.push("crs", "25391");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_I__PRS92__PHILIPPINES_ZONE_1__3121.SP":
                MyData.push("crs", "3121");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_V_ONSHORE__LUZON_1911__PHILIPPINES_ZONE_V__25395.SP":
                MyData.push("crs", "25395");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PHILIPPINES__ZONE_V__PRS92__PHILIPPINES_ZONE_5__3125.SP":
                MyData.push("crs", "3125");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}