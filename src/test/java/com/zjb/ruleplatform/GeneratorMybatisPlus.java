package com.zjb.ruleplatform;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author 赵静波
 * @date 2021-01-27 18:29:32
 */
public class GeneratorMybatisPlus {

    @Test
    public void generateActivityCode() {
        String packageName = "com.zjb.ruleplatform";
        boolean serviceNameStartWithI = false;//user -> UserService, 设置成true: user -> IUserService

        String dbUrl = "jdbc:mysql://47.92.98.61:3306/rule";
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig
                .setUrl(dbUrl)
                .setUsername("root")
                .setPassword("OPBXj1wNCXYKoS2*")
                .setDriverName("com.mysql.cj.jdbc.Driver");

        generateByTables("",serviceNameStartWithI,dataSourceConfig, packageName, "rule_engine_rule_set");
        //generateByTables("",serviceNameStartWithI,dataSourceConfig, packageName, "\\w+engine\\w+");
    }








    private void generateByTables(String subPackage,boolean serviceNameStartWithI, DataSourceConfig dataSourceConfig, String packageName, String... tableNames) {


        StrategyConfig strategyConfig = new StrategyConfig();

        //TableFill createTime=new TableFill("create_time", FieldFill.INSERT);
        //TableFill updateTime=new TableFill("update_time", FieldFill.INSERT_UPDATE);
        //TableFill deleted=new TableFill("deleted", FieldFill.INSERT);
        strategyConfig
                .setEntityBooleanColumnRemoveIsPrefix(true)
                .setCapitalMode(true)
                .setEntityLombokModel(true)
                .setLogicDeleteFieldName("deleted")
                .setNaming(NamingStrategy.underline_to_camel)

                //.setTableFillList(Arrays.asList(createTime,updateTime,deleted))
                .setInclude(tableNames);//修改替换成你需要的表名，多个表名传数组

        GlobalConfig config = new GlobalConfig();
        config.setActiveRecord(false)
                .setDateType(DateType.ONLY_DATE)
                .setAuthor("zhaojingbo")
                .setOutputDir(System.getProperty("user.dir")+File.separator+"src"+File.separator+"main"+File.separator+"java")
                .setFileOverride(true)/*.setBaseColumnList(true).setBaseResultMap(true)*/
        .setOpen(false)
        ;

                // .setFileOverride(true);
        if (!serviceNameStartWithI) {
            config.setServiceName("%sManager");
            config.setServiceImplName("%sManagerImpl");
        }
        new AutoGenerator().setGlobalConfig(config)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(
                        new PackageConfig()
                                .setParent(packageName)
                                .setMapper("mapper")
                                .setXml("mapper")
                                .setEntity("entity")
                                .setService("manager")
                                .setServiceImpl("manager.impl")
                                .setController("controller")
                                // .setMapper("mapper.activity")
                                // .setXml("mapper.activity")
                                // .setEntity("entity.domain.activity")
                ).execute();

    }


    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }
}
