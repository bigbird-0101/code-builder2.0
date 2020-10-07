package main.java.orgv1;

import com.mysql.jdbc.PreparedStatement;
import main.java.domain.DataSourceConfig;
import main.java.common.Utils;
import main.java.domain.FileTempleConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 把数据库中的表转化为java对象
 *
 * @author fpp
 */
public class TableToJavaTool {

    /**
     * 数据源配置
     */
    private DataSourceConfig dataSourceConfigPojo;
    /**
     * 文件模板配置
     */
    private FileTempleConfig fileTempleConfigPojo;

    /**
     * 构建代码的方式
     * 默认创建新文件 把代码写入
     */
    private BuildCodeType buildCodeType;

    private List<String> buildEdFileUrlList = new ArrayList<>();

    public List<String> getBuildEdFileUrlList() {
        return buildEdFileUrlList;
    }

    /**
     * 字段的分隔符
     */
    private static final String FILED_SPLIT = ",";

    private static final String FILE_PATH_SPLIT = "/";
    /**
     * 表名
     */
    public static String tableName;
    /**
     * 表注释
     */
    private String tableComment;
    /**
     * 表类别名称
     */
    private String catalog;


    private final List<String> primaryKeyList = new ArrayList<String>();

    private List<Map<String, Object>> dataList = null;
    private Set<String> uniqueKeyMap = new HashSet<>();

    public TableToJavaTool(DataSourceConfig dataSourceConfigPojo, FileTempleConfig fileTempleConfigPojo, BuildCodeType buildCodeType) {
        this.dataSourceConfigPojo = dataSourceConfigPojo;
        this.fileTempleConfigPojo = fileTempleConfigPojo;
        this.buildCodeType = buildCodeType;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }

    public List<String> getPrimaryKeyList() {
        return primaryKeyList;
    }

    public void process() throws SQLException, ClassNotFoundException, IOException {
        List<String> tableNameS = getAllTableName();

        for (String tableName : tableNameS) {
            process(tableName);
        }
    }

    public void process(String tableName) throws SQLException, ClassNotFoundException, IOException {
        checkData(tableName);
        //获取所有表注释
        getTableComment(tableName);
        String srcUrl = fileTempleConfigPojo.getSrcUrl();
        String completeUrl = fileTempleConfigPojo.getProjectUrl() + FILE_PATH_SPLIT + fileTempleConfigPojo.getSrcUrlPrefix() + FILE_PATH_SPLIT + srcUrl;
        //校验完整地址是否真实存在
        dataList = readData(tableName);
        //生成bean文件
        int fileType = fileTempleConfigPojo.getTypeBuild();
        if ((fileType & FileTypeEnum.DOMAIN.getType()) == FileTypeEnum.DOMAIN.getType()) {
            String domainPackage = fileTempleConfigPojo.getDomainPackage();
            createJavaBeanFile(tableName, completeUrl + FILE_PATH_SPLIT + domainPackage, getPackName(srcUrl + "/" + domainPackage));
        }

        //生成controller 文件
        if ((fileType & FileTypeEnum.CONTROLLER.getType()) == FileTypeEnum.CONTROLLER.getType()) {
            String controllerPackage = fileTempleConfigPojo.getControllerPackage();
            createJavaControllerFile(tableName, completeUrl + FILE_PATH_SPLIT + controllerPackage, getPackName(srcUrl + "/" + controllerPackage));
        }

        //生成service 文件
        if ((fileType & FileTypeEnum.SERVICE.getType()) == FileTypeEnum.SERVICE.getType()) {
            String servicePackage = fileTempleConfigPojo.getServicePackage();
            createJavaServiceFile(tableName, completeUrl + FILE_PATH_SPLIT + servicePackage, getPackName(srcUrl + "/" + servicePackage));
        }

        //生成serviceImpl 文件
        if ((fileType & FileTypeEnum.SERVICE.getType()) == FileTypeEnum.SERVICE.getType()) {
            String serviceImplPackage = fileTempleConfigPojo.getServiceImplPackage();
            createJavaServiceImplFile(tableName, completeUrl + FILE_PATH_SPLIT + serviceImplPackage, getPackName(srcUrl + "/" + serviceImplPackage));
        }

        //生成Dao 文件
        if ((fileType & FileTypeEnum.DAO.getType()) == FileTypeEnum.DAO.getType()) {
            String daoPackage = fileTempleConfigPojo.getDaoPackage();
            createJavaDaoFile(tableName, completeUrl + FILE_PATH_SPLIT + daoPackage, getPackName(srcUrl + "/" + daoPackage));
        }

    }

    /**
     * 数据校验
     */
    private void checkData(String tableName) throws SQLException, ClassNotFoundException {
        if (Utils.isEmpty(tableName)) {
            throw new NullPointerException("目标表名不允许为空!");
        }
        if (!tableIsExists(tableName)) {
            throw new NullPointerException("表名不存在!");
        }
        int functionType = fileTempleConfigPojo.getFunctionBuild();
        if ((functionType & FunctionTypeEnum.GET_BY_FILED.getType()) == FunctionTypeEnum.GET_BY_FILED.getType()) {
            String filedS = fileTempleConfigPojo.getGetByFiledFiledS();
            if (Utils.isEmpty(filedS)) {
                throw new NullPointerException("请输入表字段!");
            }

//            List<String> arrays = Arrays.stream(filedS.split(",")).filter(i -> !Utils.isEmpty(i)).collect(Collectors.toList());
//            for (String filed : arrays) {
//                Connection conn = null;
//                try {
//                    conn = getConnection();
//                    PreparedStatement pStemt = null;
//                    ResultSet rs = null;
//                    pStemt = (PreparedStatement) conn.prepareStatement("select " + filed + " from " + tableName);
//                    rs = pStemt.executeQuery();
//                    if (!rs.next()) {
//                        throw new NullPointerException(filed + "字段不存在!");
//                    }
//                } finally {
//                    try {
//                        conn.close();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
    }

    private void createJavaDaoFile(String tableName, String daoFileUrl, String packageName) throws IOException {
        //如果在目录当中已经有表名一样的目录，那么就放在该目录下面
        String parentFile = getFileInExistFile(daoFileUrl, tableName);
        String parentFileReal = Utils.isEmpty(parentFile) ? "" : "\\" + parentFile;
        daoFileUrl = daoFileUrl + parentFileReal;


        String javaBeanClassName = getJavaClassName(tableName);
        String javaDaoName = javaBeanClassName + "Dao";


        String lowerCaseBeanName = firstLowerCase(javaBeanClassName);

        //获取表注释  如果表注释最后一个字有表字那么去除这个字
        String tableComment = this.tableComment;
        if (!Utils.isEmpty(tableComment) && "表".equals(tableComment.substring(tableComment.length() - 1))) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }

        // 获取文件下的定义的包名
        StringBuilder jbString = new StringBuilder();
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("package ").append(packageName).append(";").append("\r\n\n");

            //导入jar包
            //bean 包
            String srcUrl = fileTempleConfigPojo.getSrcUrl();
            String beanImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getDomainPackage()) + "." + (Utils.isEmpty(parentFile) ? "" : parentFile + ".") + "" + javaBeanClassName + ";\r\n";
            jbString.append(beanImportJar)
                    .append("import java.util.List;\r\n" +
                            "\r\n" +
                            "import org.apache.ibatis.annotations.Delete;\r\n" +
                            "import org.apache.ibatis.annotations.Insert;\r\n" +
                            "import org.apache.ibatis.annotations.Mapper;\r\n" +
                            "import org.apache.ibatis.annotations.One;\r\n" +
                            "import org.apache.ibatis.annotations.Param;\r\n" +
                            "import org.apache.ibatis.annotations.Result;\r\n" +
                            "import org.apache.ibatis.annotations.Results;\r\n" +
                            "import org.apache.ibatis.annotations.Select;\r\n" +
                            "import org.apache.ibatis.annotations.Update;\r\n" +
                            "import org.apache.ibatis.mapping.FetchType;\r\n");
            //拼接类的定义
            jbString.append("/**\r\n" +
                    " * " + tableComment + "业务处理接口DB类 \r\n" +
                    " * @author fpp\r\n" +
                    " */\r\n")
                    .append("@Mapper\r\n")
                    .append("public interface ").append(javaDaoName).append("\r\n")
                    .append("{").append("\r\n");
        }
        int functionType = fileTempleConfigPojo.getFunctionBuild();
        if ((functionType & FunctionTypeEnum.ADD.getType()) == FunctionTypeEnum.ADD.getType()) {
            jbString.append("	/**\r\n" +
                    "    * 添加" + tableComment + "\r\n" +
                    "    * @param " + lowerCaseBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Insert({\r\n" +
                    "   " + getAddSql(tableName) + "" +
                    "   })\r\n" +
                    "	int add" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerCaseBeanName + ");\r\n");
        }
        if ((functionType & FunctionTypeEnum.DELETE.getType()) == FunctionTypeEnum.DELETE.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 删除" + tableComment + " 根据其ID数组\r\n" +
                    "	 * @param idArray " + tableComment + "ID数组\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Delete({\r\n" +
                    "   " + getDeleteSql(tableName) + "" +
                    "   })\r\n" +
                    "	int delete" + javaBeanClassName + "(List<String> idArray);\r\n");
        }
        if ((functionType & FunctionTypeEnum.EDIT.getType()) == FunctionTypeEnum.EDIT.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 编辑" + tableComment + "\r\n" +
                    "	 * @param " + lowerCaseBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Update({\r\n" +
                    "   " + getEditSql(tableName) + "" +
                    "   })\r\n" +
                    "	int edit" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerCaseBeanName + ");\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_ID.getType()) == FunctionTypeEnum.GET_BY_ID.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据ID获取" + tableComment + "信息\r\n" +
                    "	 * @param id " + tableComment + "ID\r\n" +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "   @Select({\r\n" +
                    "   " + getSelectByIdSql(tableName) + "" +
                    "   })\r\n" +
                    "	" + javaBeanClassName + " get" + javaBeanClassName + "ById(@Param(\"id\") int id);\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_FILED.getType()) == FunctionTypeEnum.GET_BY_FILED.getType()) {
            String fieldS = fileTempleConfigPojo.getGetByFiledFiledS();
            jbString.append("	/**\r\n" +
                    "	 * 根据" + fieldS + "获取" + tableComment + "信息\r\n" +
                    getFiledInFunctionCommentWhenMultiFiled(fieldS) +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "   @Select({\r\n" +
                    "   " + getSelectByFiledSql(tableName, fieldS) + "" +
                    "   })\r\n" +
                    "	" + javaBeanClassName + " get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFunctionParamInDaoFunctionWhenMultiFiled(fieldS) + ")" + ";\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据所属者id和关键字获取" + tableComment + "信息个数\r\n" +
                    "	 * @param belongToId 所属者ID\r\n" +
                    "	 * @param findKey 查找关键字\r\n" +
                    "	 * @return 查找到的记录总数\r\n" +
                    "    */\r\n" +
                    "   @Select({\r\n" +
                    "   " + getSelectCountSql(tableName) + "" +
                    "   })\r\n" +
                    "	int get" + javaBeanClassName + "Count(@Param(\"belongToId\")Integer belongToId,@Param(\"findKey\")String findKey);\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            jbString.append("	/**\r\b" +
                    "	 * 分页方法获取" + tableComment + "信息列表\r\n" +
                    "    * @param belongToId " + tableComment + "所属者的ID\r\n" +
                    "    * @param findKey " + tableComment + " 信息关键字\r\n" +
                    "    * @param offset 当前页数\r\n" +
                    "    * @param pageCount 当前页总数\r\n" +
                    "    * @return " + tableComment + "数据列表\r\n" +
                    "    */\r\n" +
                    "   @Select({\r\n" +
                    "   " + getSelectListSql(tableName) + "" +
                    "   })\r\n" +
                    "	List<" + javaBeanClassName + "> getAll" + javaBeanClassName + "List(@Param(\"belongToId\")Integer belongToId,@Param(\"findKey\")String findKey,@Param(\"offset\")int offset,@Param(\"pageCount\")int pageCount);\r\n");
        }
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("}\r\n");
            jbString.append("//");
        }
        buildTargetFile(jbString.toString(), daoFileUrl + "\\" + javaDaoName);
    }

    private void createJavaServiceImplFile(String tableName, String serviceImplFileUrl, String packageName) throws IOException {
        //如果在目录当中已经有表名一样的目录，那么就放在该目录下面
        String parentFile = getFileInExistFile(serviceImplFileUrl, tableName);
        String parentFileReal = Utils.isEmpty(parentFile) ? "" : "\\" + parentFile;
        serviceImplFileUrl = serviceImplFileUrl + parentFileReal;

        String javaBeanClassName = getJavaClassName(tableName);
        String javaServiceName = javaBeanClassName + "Service";
        String javaServiceImplName = javaBeanClassName + "ServiceImpl";
        String javaDaoName = javaBeanClassName + "Dao";

        String lowerDaoCaseName = firstLowerCase(javaDaoName);
        String lowerBeanName = firstLowerCase(javaBeanClassName);
        //获取表注释  如果表注释最后一个字有表字那么去除这个字
        String tableComment = this.tableComment;
        if (!Utils.isEmpty(tableComment) && "表".equals(tableComment.substring(tableComment.length() - 1))) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }

        // 获取文件下的定义的包名
        StringBuffer jbString = new StringBuffer();
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("package ").append(packageName + ";").append("\r\n\n");

            //导入jar包
            //bean 包
            String srcUrl = fileTempleConfigPojo.getSrcUrl();
            String beanImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getDomainPackage()) + "." + javaBeanClassName + ";\r\n";
            String daoImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getDaoPackage()) + "." + javaDaoName + ";\r\n";
            jbString.append(beanImportJar)
                    .append(daoImportJar)
                    .append("import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getServicePackage()) + "." + javaServiceName + ";\r\n")
                    .append("import java.util.Arrays;\r\n")
                    .append("import java.util.Objects;\r\n")
                    .append("import java.util.stream.Collectors;\r\n")
                    .append("import org.apache.logging.log4j.util.Strings;\r\n")
                    .append("import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                            "import org.springframework.stereotype.Service;\r\n" +
                            "import org.springframework.transaction.annotation.Transactional;\r\n")
                    .append("import java.util.List;\r\n");

            //拼接类的定义
            jbString.append("/**\r\n" +
                    " * " + tableComment + "业务处理接口实现类 \r\n" +
                    " * @author fpp\r\n" +
                    " */\r\n")
                    .append("@Service\r\n")
                    .append("public class ").append(javaServiceImplName + " implements " + javaServiceName + " ").append("\r\n")
                    .append("{").append("\r\n");

            //拼接全局变量
            jbString.append("   @Autowired\r\n")
                    .append("   private " + javaDaoName + " " + lowerDaoCaseName + ";\r\n");
        }
        int functionType = fileTempleConfigPojo.getFunctionBuild();
        if ((functionType & FunctionTypeEnum.ADD.getType()) == FunctionTypeEnum.ADD.getType()) {
            jbString.append("	/**\r\n" +
                    "    * 添加" + tableComment + "\r\n" +
                    "    * @param " + lowerBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "   @Transactional(rollbackFor = Throwable.class)\r\n" +
                    "	public boolean add" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerBeanName + "){\r\n")
                    .append("       Objects.requireNonNull(" + lowerBeanName + ");\r\n" +
                            "		return " + lowerDaoCaseName + ".add" + javaBeanClassName + "(" + lowerBeanName + ")==1;\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.DELETE.getType()) == FunctionTypeEnum.DELETE.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 删除" + tableComment + " 根据其ID数组\r\n" +
                    "	 * @param idS " + tableComment + "ID数组\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "   @Transactional(rollbackFor = Throwable.class)\r\n" +
                    "	public boolean delete" + javaBeanClassName + "(String idS){\r\n")
                    .append("        if(Strings.isNotBlank(idS)) {\r\n" +
                            "			List<String> idArray=Arrays.stream(idS.split(\",\")).filter(i->Strings.isNotBlank(i)).distinct().collect(Collectors.toList());\r\n" +
                            "			return " + lowerDaoCaseName + ".delete" + javaBeanClassName + "(idArray)==idArray.size();\r\n" +
                            "		}\r\n" +
                            "		return false;\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.EDIT.getType()) == FunctionTypeEnum.EDIT.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 编辑" + tableComment + "\r\n" +
                    "	 * @param " + lowerBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "   @Transactional(rollbackFor = Throwable.class)\r\n" +
                    "	public boolean edit" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerBeanName + "){\r\n")
                    .append("        Objects.requireNonNull(" + lowerBeanName + ");\r\n" +
                            "		return " + lowerDaoCaseName + ".edit" + javaBeanClassName + "(" + lowerBeanName + ")==1;\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_ID.getType()) == FunctionTypeEnum.GET_BY_ID.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据ID获取" + tableComment + "信息\r\n" +
                    "	 * @param id " + tableComment + "ID\r\n" +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "	public " + javaBeanClassName + " get" + javaBeanClassName + "ById(int id){\r\n")
                    .append("       return " + lowerDaoCaseName + ".get" + javaBeanClassName + "ById(id);\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_FILED.getType()) == FunctionTypeEnum.GET_BY_FILED.getType()) {
            String fieldS = fileTempleConfigPojo.getGetByFiledFiledS();
            jbString.append("	/**\r\n" +
                    "	 * 根据" + getFiledNameSInDoMain(fieldS) + "获取" + tableComment + "信息\r\n" +
                    getFiledInFunctionCommentWhenMultiFiled(fieldS) +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "	public " + javaBeanClassName + " get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFunctionParamWhenMultiFiled(fieldS, "", "") + "){\r\n")
                    .append("       return " + lowerDaoCaseName + ".get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFiledNameSInDoMain(fieldS) + ");\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据所属者id和关键字获取" + tableComment + "信息个数\r\n" +
                    "	 * @param findKey 查找关键字\r\n" +
                    "	 * return 查找到的记录总数\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "	public int get" + javaBeanClassName + "Count(String findKey){\r\n")
                    .append("       int belongToId=TokenSupport.currentId();\r\n")
                    .append("       return " + lowerDaoCaseName + ".get" + javaBeanClassName + "Count(belongToId,findKey);\r\n")
                    .append("	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 分页方法获取" + tableComment + "信息列表\r\n" +
                    "    * @param findKey " + tableComment + " 信息关键字\r\n" +
                    "    * @param offset 当前页数\r\n" +
                    "    * @param pageCount 当前页总数\r\n" +
                    "    * @return " + tableComment + "数据列表\r\n" +
                    "    */\r\n" +
                    "   @Override\r\n" +
                    "	public List<" + javaBeanClassName + "> getAll" + javaBeanClassName + "List(String findKey,int offset,int pageCount){\r\n")
                    .append("       int belongToId=TokenSupport.currentId();\r\n")
                    .append("       return " + lowerDaoCaseName + ".getAll" + javaBeanClassName + "List(belongToId,findKey,offset,pageCount);\r\n")
                    .append("	}\r\n");
        }
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("}\r\n");
            jbString.append("//");
        }
        buildTargetFile(jbString.toString(), serviceImplFileUrl + "\\" + javaServiceImplName);
    }

    private void createJavaServiceFile(String tableName, String serviceFileUrl, String packageName) throws IOException {
        //如果在目录当中已经有表名一样的目录，那么就放在该目录下面
        String parentFile = getFileInExistFile(serviceFileUrl, tableName);
        String parentFileReal = Utils.isEmpty(parentFile) ? "" : "\\" + parentFile;
        serviceFileUrl = serviceFileUrl + parentFileReal;

        String javaBeanClassName = getJavaClassName(tableName);
        String javaServiceName = javaBeanClassName + "Service";

        String lowerCaseBeanName = firstLowerCase(javaBeanClassName);

        //获取表注释  如果表注释最后一个字有表字那么去除这个字
        String tableComment = this.tableComment;
        if (!Utils.isEmpty(tableComment) && "表".equals(tableComment.substring(tableComment.length() - 1))) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }

        // 获取文件下的定义的包名
        StringBuilder jbString = new StringBuilder();
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("package ").append(packageName + ";").append("\r\n\n");

            //导入jar包
            //bean 包
            String srcUrl = fileTempleConfigPojo.getSrcUrl();
            String beanImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getDomainPackage()) + "." + (Utils.isEmpty(parentFile) ? "" : parentFile + ".") + "" + javaBeanClassName + ";\r\n";
            if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
                jbString.append(beanImportJar)
                        .append("import java.util.List;\r\n");

                //拼接类的定义
                jbString.append("/**\r\n" +
                        " * " + tableComment + "业务处理接口类 \r\n" +
                        " * @author fpp\r\n" +
                        " */\r\n")
                        .append("public interface ").append(javaServiceName)
                        .append("{").append("\r\n");
            }
        }
        int functionType = fileTempleConfigPojo.getFunctionBuild();
        if ((functionType & FunctionTypeEnum.ADD.getType()) == FunctionTypeEnum.ADD.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 添加" + tableComment + "\r\n" +
                    "    * @param " + lowerCaseBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "	boolean add" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerCaseBeanName + ");\r\n");
        }
        if ((functionType & FunctionTypeEnum.DELETE.getType()) == FunctionTypeEnum.DELETE.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 删除" + tableComment + " 根据其ID数组\r\n" +
                    "	 * @param idS " + tableComment + "ID数组\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "	boolean delete" + javaBeanClassName + "(String idS);\r\n");
        }
        if ((functionType & FunctionTypeEnum.EDIT.getType()) == FunctionTypeEnum.EDIT.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 编辑" + tableComment + "\r\n" +
                    "	 * @param " + lowerCaseBeanName + " " + tableComment + "POJO\r\n" +
                    "    * @return 返回影响的行数\r\n" +
                    "    */\r\n" +
                    "	boolean edit" + javaBeanClassName + "(" + javaBeanClassName + " " + lowerCaseBeanName + ");\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_ID.getType()) == FunctionTypeEnum.GET_BY_ID.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据ID获取" + tableComment + "信息\r\n" +
                    "	 * @param id " + tableComment + "ID\r\n" +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "	" + javaBeanClassName + " get" + javaBeanClassName + "ById(int id);\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_FILED.getType()) == FunctionTypeEnum.GET_BY_FILED.getType()) {
            String fieldS = fileTempleConfigPojo.getGetByFiledFiledS();
            jbString.append("	/**\r\n" +
                    "	 * 根据" + getFiledNameSInDoMain(fieldS) + "获取" + tableComment + "信息\r\n" +
                    getFiledInFunctionCommentWhenMultiFiled(fieldS) +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n" +
                    "	 " + javaBeanClassName + " get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFunctionParamWhenMultiFiled(fieldS, "", "") + ");\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            jbString.append("	/**\r\n" +
                    "	 * 根据所属者id和关键字获取" + tableComment + "信息个数\r\n" +
                    "	 * @param findKey 查找关键字\r\n" +
                    "	 * @return 查找到的记录总数\r\n" +
                    "    */\r\n" +
                    "	int get" + javaBeanClassName + "Count(String findKey);\r\n")
                    .append("	/**\r\n" +
                            "	 * 分页方法获取" + tableComment + "信息列表\r\n" +
                            "    * @param findKey " + tableComment + " 信息关键字\r\n" +
                            "    * @param offset 当前页数\r\n" +
                            "    * @param pageCount 当前页总数\r\n" +
                            "    * @return " + tableComment + "数据列表\r\n" +
                            "    */\r\n" +
                            "	List<" + javaBeanClassName + "> getAll" + javaBeanClassName + "List(String findKey,int offset,int pageCount);\r\n");
        }
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("}\r\n");
            jbString.append("//");
        }
        buildTargetFile(jbString.toString(), serviceFileUrl + "\\" + javaServiceName);
    }

    private void createJavaControllerFile(String tableName, String controllerFileUrl, String packageName) throws IOException {
        //如果在目录当中已经有表名一样的目录，那么就放在该目录下面
        String parentFile = getFileInExistFile(controllerFileUrl, tableName);
        String parentFileReal = Utils.isEmpty(parentFile) ? "" : "\\" + parentFile;
        controllerFileUrl = controllerFileUrl + parentFileReal;

        String javaBeanClassName = getJavaClassName(tableName);
        String javaClassControllerName = javaBeanClassName + "Controller";
        String javaServiceName = javaBeanClassName + "Service";

        String lowerCaseBeanName = firstLowerCase(javaBeanClassName);
        // 获取文件下的定义的包名
        StringBuffer jbString = new StringBuffer();
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("package ").append(packageName + ";").append("\r\n\n");
        }

        //获取表注释  如果表注释最后一个字有表字那么去除这个字
        String tableComment = this.tableComment;
        if (!Utils.isEmpty(tableComment) && "表".equals(tableComment.substring(tableComment.length() - 1))) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }
        //导入jar包
        //bean 包
        String srcUrl = fileTempleConfigPojo.getSrcUrl();
        String beanImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getDomainPackage()) + "." + (Utils.isEmpty(parentFile) ? "" : parentFile + ".") + "" + javaBeanClassName + ";\r\n";
        //service包
        String serviceImportJar = "import " + getPackName(srcUrl + "/" + fileTempleConfigPojo.getServicePackage()) + "." + javaServiceName + ";\r\n";

        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                    "import org.springframework.validation.BindingResult;\r\n" +
                    "import org.springframework.validation.ObjectError;\r\n" +
                    "import org.springframework.web.bind.annotation.RestController;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestMethod;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestParam;\r\n" +
                    "import org.springframework.validation.annotation.Validated;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestBody;\r\n")
                    .append(beanImportJar + "\r\n")
                    .append(serviceImportJar + "\r\n")
                    .append("import java.util.List;\r\n" +
                            "import java.util.Objects;\r\n")
                    .append("import io.swagger.annotations.Api;\r\n" +
                            "import io.swagger.annotations.ApiImplicitParam;\r\n" +
                            "import io.swagger.annotations.ApiImplicitParams;\r\n" +
                            "import io.swagger.annotations.ApiOperation;\r\n");

            //拼接类的定义
            jbString.append("/**\r\n" +
                    " * " + tableComment + "类 \r\n" +
                    " * @author fpp\r\n" +
                    " */\r\n")
                    .append("@RestController\r\n" +
                            "@RequestMapping(\"/controller/" + javaBeanClassName.toLowerCase() + "/" + javaClassControllerName.toLowerCase() + "/\")\r\n")
                    .append("@Api(value = \"" + javaClassControllerName + "|" + tableComment + "控制器\")\r\n")
                    .append("public class ").append(javaClassControllerName).append("\r\n")
                    .append("{").append("\r\n");

            //拼接全局变量
            jbString.append("    /**\r\n")
                    .append("     *业务处理类注入\r\n")
                    .append("     */\r\n")
                    .append("    @Autowired\r\n")
                    .append("    private " + javaServiceName + " " + firstLowerCase(javaServiceName) + ";\r\n\n");
        }

        int functionType = fileTempleConfigPojo.getFunctionBuild();
        //拼接增加 删除 、 修改、根据id获取bean,查找所有的记录(包含分页)
        if ((functionType & FunctionTypeEnum.ADD.getType()) == FunctionTypeEnum.ADD.getType()) {
            //增加
            jbString.append("    /**\r\n" +
                    "    * 添加" + tableComment + "\r\n" +
                    "    * @param " + firstLowerCase(javaBeanClassName) + " " + tableComment + "信息 \r\n" +
                    "    * @param errorResult 错误结果信息\r\n" +
                    "    * @return 数据处理结果\r\n" +
                    "    */\r\n")
                    .append("    @PostMapping(\"/add" + javaBeanClassName + "\")\r\n")
                    .append("    @ApiOperation(value=\"添加" + tableComment + "\", notes=\"\")\r\n")
                    .append("    public ReturnValue add" + javaBeanClassName + "(@Validated @RequestBody " + javaBeanClassName + " " + firstLowerCase(javaBeanClassName) + ",BindingResult errorResult) {\r\n" +
                            "			List<ObjectError> errorList=errorResult.getAllErrors();\r\n" +
                            "			if(!errorList.isEmpty()) {\r\n" +
                            "				String message=errorList.stream().findFirst().get().getDefaultMessage();\r\n" +
                            "				return ReturnValueFactory.buildDataReturnValue(CommonExceptionCodeEnum.FORM_DATA_ERROR,message);\r\n" +
                            "			}\r\n" +
                            "		     return ReturnValueFactory.buildReturnValue(" + lowerCaseBeanName + "Service.add" + javaBeanClassName + "(" + lowerCaseBeanName + "));\r\n" +
                            "	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.DELETE.getType()) == FunctionTypeEnum.DELETE.getType()) {
            //删除
            jbString.append("    /**\r\n" +
                    "    * 删除" + tableComment + "根据ID数组\r\n" +
                    "    * @param idS " + tableComment + "ID数组 \r\n" +
                    "    * @return 数据处理结果\r\n" +
                    "    */\r\n")
                    .append("    @DeleteMapping(\"/delete" + javaBeanClassName + "\")\r\n")
                    .append("    @ApiOperation(value=\"删除" + tableComment + "\", notes=\"\")\r\n")
                    .append("    @ApiImplicitParam(paramType=\"query\", name = \"idS\", value = \"" + tableComment + "ID数组\", required = true, dataType = \"String\")\r\n")
                    .append("    public ReturnValue delete" + javaBeanClassName + "(@RequestParam String idS) {\r\n" +
                            "		     return ReturnValueFactory.buildReturnValue(" + lowerCaseBeanName + "Service.delete" + javaBeanClassName + "(idS));\r\n" +
                            "	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.EDIT.getType()) == FunctionTypeEnum.EDIT.getType()) {
            //修改
            jbString.append("    /**\r\n" +
                    "    * 修改" + tableComment + "\r\n" +
                    "    * @param " + firstLowerCase(javaBeanClassName) + " " + tableComment + "信息 \r\n" +
                    "    * @param errorResult 错误结果信息\r\n" +
                    "    * @return 数据处理结果\r\n" +
                    "    */\r\n")
                    .append("    @PutMapping(\"/edit" + javaBeanClassName + "\")\r\n")
                    .append("    @ApiOperation(value=\"修改" + tableComment + "\", notes=\"\")\r\n")
                    .append("    public ReturnValue edit" + javaBeanClassName + "(@Validated @RequestBody " + javaBeanClassName + " " + firstLowerCase(javaBeanClassName) + ",BindingResult errorResult) {\r\n" +
                            "			List<ObjectError> errorList=errorResult.getAllErrors();\r\n" +
                            "			if(!errorList.isEmpty()) {\r\n" +
                            "				String message=errorList.stream().findFirst().get().getDefaultMessage();\r\n" +
                            "				return ReturnValueFactory.buildDataReturnValue(CommonExceptionCodeEnum.FORM_DATA_ERROR,message);\r\n" +
                            "			}\r\n" +
                            "		     return ReturnValueFactory.buildReturnValue(" + lowerCaseBeanName + "Service.edit" + javaBeanClassName + "(" + lowerCaseBeanName + "));\r\n" +
                            "	}\r\n");

        }
        if ((functionType & FunctionTypeEnum.GET_BY_ID.getType()) == FunctionTypeEnum.GET_BY_ID.getType()) {
            //根据ID获取bean
            jbString.append("    /**\r\n" +
                    "    * 根据ID 获取" + tableComment + "\r\n" +
                    "    * @param id " + tableComment + "的唯一ID\r\n" +
                    "    * @return 数据处理结果\r\n" +
                    "    */\r\n")
                    .append("    @GetMapping(\"/get" + javaBeanClassName + "ById\")\r\n")
                    .append("    @ApiOperation(value=\"根据id获取" + tableComment + "\", notes=\"返回" + tableComment + "详细信息\")\r\n")
                    .append("    @ApiImplicitParam(paramType=\"query\", name = \"id\", value = \"" + tableComment + "ID\", required = true, dataType = \"Integer\")\r\n")
                    .append("    public ReturnValue get" + javaBeanClassName + "ById(@RequestParam Integer id) {\r\n" +
                            "		    return ReturnValueFactory.buildSuccessDataReturnValue(" + lowerCaseBeanName + "Service.get" + javaBeanClassName + "ById(id));\r\n" +
                            "	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_BY_FILED.getType()) == FunctionTypeEnum.GET_BY_FILED.getType()) {
            String fieldS = fileTempleConfigPojo.getGetByFiledFiledS();
            jbString.append("	/**\r\n" +
                    "	 * 根据" + getFiledNameSInDoMain(fieldS) + "获取" + tableComment + "信息\r\n" +
                    getFiledInFunctionCommentWhenMultiFiled(fieldS) +
                    "    * @return " + tableComment + "POJO\r\n" +
                    "    */\r\n")
                    .append("    @GetMapping(\"/get" + javaBeanClassName + "By"+getFiledFiledSFunctionName(fieldS)+"\")\r\n")
                    .append("    @ApiOperation(value=\"根据" + getFiledNameSInDoMain(fieldS) + "获取" + tableComment + "\", notes=\"返回" + tableComment + "详细信息\")\r\n")
                    .append(getSwaggerApiParamWhenMultiFiled(fieldS))
                    .append("    public ReturnValue get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFunctionParamInControllerFunctionWhenMultiFiled(fieldS) + ")" + " {\r\n" +
                            "		    return ReturnValueFactory.buildSuccessDataReturnValue(" + lowerCaseBeanName + "Service.get" + javaBeanClassName + "By" + getFiledFiledSFunctionName(fieldS) + "(" + getFiledNameSInDoMain(fieldS) + ")" + ");\r\n" +
                            "	}\r\n");
        }
        if ((functionType & FunctionTypeEnum.GET_ALL.getType()) == FunctionTypeEnum.GET_ALL.getType()) {
            //分页查询方法
            jbString.append("    /**\r\n" +
                    "    * 分页方法\r\n" +
                    "    * 获取" + tableComment + "列表信息\r\n" +
                    "    * @param findKey " + tableComment + " 信息关键字\r\n" +
                    "    * @param indexOfPage 当前页数\r\n" +
                    "    * @param pageCount 当前页总数\r\n" +
                    "    * @return 数据处理结果\r\n" +
                    "    */\r\n")
                    .append("    @GetMapping(\"/getAll" + javaBeanClassName + "List\")\r\n")
                    .append("    @ApiOperation(value=\"获取" + tableComment + "列表\", notes=\"返回" + tableComment + "列表信息\")\r\n")
                    .append("    @ApiImplicitParams({\r\n" +
                            "        @ApiImplicitParam(paramType=\"query\", name = \"findKey\", value = \"" + tableComment + "信息关键字\", required = true, dataType = \"String\"),\r\n" +
                            "        @ApiImplicitParam(paramType=\"query\", name = \"indexOfPage\", value = \"当前页数,如果不传默认为1\", required = false, dataType = \"Integer\"),\r\n" +
                            "        @ApiImplicitParam(paramType=\"query\", name = \"pageCount\", value = \"每页记录数，如果不传默认为20\", required = false, dataType = \"Integer\"),\r\n" +
                            "    })\r\n")
                    .append("    public ReturnValue getAll" + javaBeanClassName + "List(@RequestParam(required=false,defaultValue=\"\") String findKey,@RequestParam(required = false)Integer indexOfPage,@RequestParam(required = false)Integer pageCount) {\r\n" +
                            "           //获取当前记录的总数\r\n" +
                            "           indexOfPage = null == indexOfPage ? 1 : indexOfPage;\r\n" +
                            "           pageCount = null == pageCount ? 20 : pageCount;\r\n" +
                            "           //根据页码得到当前需要显示的记录数\r\n" +
                            "           int offset=(indexOfPage-1)*pageCount;\n" +
                            "    		int " + lowerCaseBeanName + "Count=" + lowerCaseBeanName + "Service.get" + javaBeanClassName + "Count(findKey);\r\n" +
                            "			List<" + javaBeanClassName + "> result=" + lowerCaseBeanName + "Service.getAll" + javaBeanClassName + "List(findKey,offset,pageCount);\r\n" +
                            "           JSONObject resultReal = new JSONObject();\n" +
                            "           resultReal.put(\"" + lowerCaseBeanName + "S" + "\", result);\n" +
                            "           resultReal.put(\"total\", " + lowerCaseBeanName + "Count);\n" +
                            "		    return ReturnValueFactory.buildSuccessDataReturnValue(resultReal);\r\n" +
                            "	}\r\n");
        }
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            jbString.append("}\r\n");
            jbString.append("//");
        }

        buildTargetFile(jbString.toString(), controllerFileUrl + "\\" + javaClassControllerName);

    }


    public void createJavaBeanFile(String tableName, String fileUrl, String packageName) throws IOException {
        //如果在目录当中已经有表名一样的目录，那么就放在该目录下面
        String parentFile = getFileInExistFile(fileUrl, tableName);
        String parentFileReal = Utils.isEmpty(parentFile) ? "" : "\\" + parentFile;
        fileUrl = fileUrl + parentFileReal;

        //获取表注释  如果表注释最后一个字有表字那么去除这个字
        String tableComment = this.tableComment;
        if (!Utils.isEmpty(tableComment) && "表".equals(tableComment.substring(tableComment.length() - 1))) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }

        String javaClassName = getJavaClassName(tableName);
        // 获取文件下的定义的包名
        StringBuffer jbString = new StringBuffer();
        jbString.append("package ").append(packageName + ";").append("\r\n\n");

        // 导入jar包
        if (dataList != null) {
            boolean flag = true;
            for (Map<String, Object> map : dataList) {
                String javaType = getJavaType((Integer) map.get("dataType"));
                if ("Date".equals(javaType)) {
                    if (!jbString.toString().contains("import java.util.Date;")) {
                        flag = false;
                        jbString.append("import java.util.Date;").append("\r\n");
                    }
                }

            }
            jbString.append("import io.swagger.annotations.ApiModel;\r\n" +
                    "import io.swagger.annotations.ApiModelProperty;\r\n");
            if (!flag) {
                jbString.append("\n");
            }
        }
        jbString.append("/**\n" +
                " * " + tableComment + "POJO\n" +
                " * @author fpp\n" +
                " */\r\n");
        jbString.append("@ApiModel(value=\"" + tableComment + "对象模型\")\r\n");
        jbString.append("public class ").append(javaClassName).append("\r\n");
        jbString.append("{").append("\r\n");

        if (dataList != null) {
            for (Map<String, Object> map : dataList) {
                String fieldName = getFieldName((String) map.get("columnName"));
                String javaType = getJavaType((Integer) map.get("dataType"));
                jbString.append("    /**").append("\r\n");
                jbString.append("      * ").append(map.get("remarks")).append("\r\n");
                jbString.append("      */\r\n");
                jbString.append("    @ApiModelProperty(value=\"" + map.get("remarks") + "\")\r\n");
                jbString.append("    private ").append(javaType).append(" ").append(fieldName).append(";")
                        .append("\r\n");
                jbString.append("\r\n");
            }

            for (Map<String, Object> map : dataList) {
                String fieldName = getFieldName((String) map.get("columnName"));
                String javaType = getJavaType((Integer) map.get("dataType"));
                jbString.append("    public ").append(javaType).append(" get").append(firstUpperCase(fieldName))
                        .append("()").append("\r\n");
                jbString.append("    {").append("\r\n");
                jbString.append("        return ").append(fieldName).append(";").append("\r\n");
                jbString.append("    }").append("\r\n");

                jbString.append("\r\n");

                jbString.append("    public void set").append(firstUpperCase(fieldName)).append("(").append(javaType)
                        .append(" ").append(fieldName).append(")").append("\r\n");
                jbString.append("    {").append("\r\n");
                jbString.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";")
                        .append("\r\n");
                jbString.append("    }").append("\r\n");

                jbString.append("\r\n");

            }
            jbString.append("\r\n");
        }

        jbString.append("}\r\n");
        jbString.append("//");

        buildTargetFile(jbString.toString(), fileUrl + "\\" + javaClassName);
    }


    public void buildTargetFile(String fileStr, String fileUrl) throws IOException {
        if (buildCodeType.compareTo(BuildCodeType.BUILD_NEW_FILE) == 0) {
            buildTargetFileNewFile(fileUrl, fileStr);
        } else if (buildCodeType.compareTo(BuildCodeType.AFTER_FILE) == 0) {
            buildTargetFileOnAfterAppendCode(fileUrl, fileStr);
        }
    }

    /**
     * 创建新的文件
     *
     * @param filePath
     * @param appendCode
     */
    public void buildTargetFileNewFile(String filePath, String appendCode) throws IOException {
        String fileName = filePath + ".java";
        File a = new File(fileName);
        if (a.exists()) {
            fileName = filePath + "_1.java";
            a = new File(fileName);
        }
        FileUtils.forceMkdirParent(a);
        FileOutputStream fops = new FileOutputStream(a);
        fops.write(Utils.formJava(appendCode).getBytes("utf-8"));
        fops.flush();
        fops.close();
        buildEdFileUrlList.add(fileName);
    }

    /**
     * 在文件的末尾处添加代码
     *
     * @param filePath
     * @param appendCode
     * @throws IOException
     */
    public void buildTargetFileOnAfterAppendCode(String filePath, String appendCode) throws IOException {
        // 创建文件
        String fileName = filePath + ".java";
        File file = new File(fileName);
        if (!file.exists()) {
            throw new IOException("文件名不存在" + fileName);
        }
        file.setWritable(true, false);
        InputStream inputStream = new FileInputStream(file);
        String s = IOUtils.toString(inputStream, "UTF-8");
        inputStream.close();
        String result = s.substring(0, s.lastIndexOf("}"));
        String relaResult = result + appendCode + "}\r\n";
        OutputStream outputStream = new FileOutputStream(file);
        IOUtils.write(relaResult, outputStream, "UTF-8");
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 获取数据库连接
     *
     * @return Connection 数据库连接对象
     */
    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Connection conn;
        String quDongName = dataSourceConfigPojo.getQuDongName();
        String user = dataSourceConfigPojo.getUserName();
        String password = dataSourceConfigPojo.getPassword();
        String url = dataSourceConfigPojo.getUrl();
        Properties props = new Properties();
        props.put("remarksReporting", "true");
        props.put("user", user);
        props.put("password", password);
        Class.forName(quDongName);
        conn = DriverManager.getConnection(url, props);
        return conn;
    }

    /**
     * 获取数据库指定表的列信息
     *
     * @param tableName 表名
     * @return List<Map < String, Object>> 列信息列表
     */
    private List<Map<String, Object>> readData(String tableName) throws SQLException, ClassNotFoundException {
        Connection conn = getConnection();
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

        try {
            DatabaseMetaData dbmd = conn.getMetaData();

            ResultSet rs = dbmd.getColumns(getCatalog(), null, tableName, null);
            Map<String, Object> map = null;

            while (rs.next()) {
                map = new HashMap<>();
                map.put("columnName", rs.getString("COLUMN_NAME"));
                map.put("dataType", rs.getInt("DATA_TYPE"));
                map.put("remarks", rs.getString("REMARKS"));
                map.put("nullAble", rs.getInt("NULLABLE"));
                map.put("size", rs.getString("COLUMN_SIZE"));
                dataList.add(map);
            }

            ResultSet rs1 = dbmd.getPrimaryKeys(getCatalog(), null, tableName);
            while (rs1.next()) {
                primaryKeyList.add(rs1.getString("COLUMN_NAME"));
            }
            //mysql数据库
            ResultSet indexInfo = dbmd.getIndexInfo(null, null, tableName, false, false);
            while (indexInfo.next()) {
                String indexName = indexInfo.getString("INDEX_NAME");
                //如果为真则说明索引值不唯一，为假则说明索引值必须唯一。
                boolean nonUnique = indexInfo.getBoolean("NON_UNIQUE");
                if (!nonUnique) {
                    uniqueKeyMap.add(indexName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }

    public static String getPackName(String fileUrl) {
        String result = "";
        if (!Utils.isEmpty(fileUrl)) {
            result = fileUrl.replaceAll("\\\\", ".").replaceAll("/", ".").replaceAll("//", ".");
        }
        return result;
    }


    private String getJavaClassName(String tableName) {
        String[] valueClassNameS = tableName.substring(4).split("_");
        return Arrays.stream(valueClassNameS).map(s -> firstUpperCase(s)).reduce((s, b) -> s + b).get();
    }

    public static String getFileInExistFile(String fileUrl, String tableName) {
        File rootFile = new File(fileUrl);
        if (rootFile.exists()) {
            return Arrays.stream(Objects.requireNonNull(rootFile.list())).filter(s -> s.contains(tableName.toLowerCase())).distinct().findAny().orElse(null);
        }
        return null;
    }

    /**
     * 把以_分隔的列明转化为字段名,将大写的首字母变小写
     *
     * @param columnName 列名
     * @return String 字段名
     */
    private static String getFieldName(String columnName) {
        if (columnName == null) {
            return "";
        }

        StringBuffer fieldNameBuffer = new StringBuffer();

        boolean nextUpperCase = false;
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);

            if (nextUpperCase) {
                fieldNameBuffer.append(columnName.substring(i, i + 1).toUpperCase());
            } else {
                fieldNameBuffer.append(c);
            }

            if (c == '_') {
                nextUpperCase = true;
            } else {
                nextUpperCase = false;
            }
        }

        String fieldName = fieldNameBuffer.toString();
        fieldName = fieldName.replaceAll("_", "");

        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        return fieldName;
    }

    /**
     * 字符串的第一个字母大写
     *
     * @param str 字符串
     * @return String 处理后的字符串
     */
    private static String firstUpperCase(String str) {
        if (str == null || str.trim().equals("")) {
            return "";
        }

        if (str.length() == 1) {
            str = str.toUpperCase();
        } else {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
    }

    /**
     * 字符串的第一个字母小写
     *
     * @param str 字符串
     * @return String 处理后的字符串
     */
    private static String firstLowerCase(String str) {
        if (str == null) {
            return "";
        }

        if (str.length() == 1) {
            str = str.toLowerCase();
        } else {
            str = str.substring(0, 1).toLowerCase() + str.substring(1);
        }
        return str;
    }


    /**
     * 将数据库列类型转换为java数据类型
     *
     * @param dataType 列类型
     * @return String java数据类型
     */
    private static String getJavaType(int dataType) {
        String javaType = "";
        if (dataType == Types.INTEGER || dataType == Types.SMALLINT) {
            javaType = "Integer";
        } else if (dataType == Types.BIGINT) {
            javaType = "Long";
        } else if (dataType == Types.CHAR || dataType == Types.VARCHAR || dataType == Types.NVARCHAR
                || dataType == Types.CLOB || dataType == Types.BLOB) {
            javaType = "String";
        } else if (dataType == Types.TINYINT) {
            javaType = "Short";
        } else if (dataType == Types.FLOAT) {
            javaType = "float";
        } else if (dataType == Types.NUMERIC || dataType == Types.DECIMAL || dataType == Types.DOUBLE) {
            javaType = "BigDecimal";
        } else if (dataType == Types.DATE || dataType == Types.TIMESTAMP || dataType == Types.TIME) {
            javaType = "String";
        } else {
            javaType = "String";
        }
        return javaType;
    }

	/*public void createMybatisColumnConfig(String tableName) {
		StringBuffer buffer = new StringBuffer();
		if (dataList != null) {
			buffer.append("<resultMap id=\"BaseResultMap\" type=\"").append(tableName).append("\"> ").append("\r\n");
			for (Map<String, Object> map : dataList) {
				// <result column="CI_TYP" jdbcType="CHAR" property="ciTyp" />
				String columnName = (String) map.get("columnName");
				String fieldName = getFieldName(columnName);
				String jdbcType = getMybatisJdbcType((Integer) map.get("dataType"));

				if (primaryKeyList.contains(columnName)) {
					buffer.append("    <id column=\"").append(columnName).append("\" ").append("jdbcType=\"")
							.append(jdbcType).append("\" property=\"").append(fieldName).append("\" />").append("\r\n");
				} else {
					buffer.append("    <result column=\"").append(columnName).append("\" ").append("jdbcType=\"")
							.append(jdbcType).append("\" property=\"").append(fieldName).append("\" />").append("\r\n");
				}
			}
			buffer.append("</resultMap>").append("\r\n");
		}

		buffer.append("<sql id=\"BaseColumnList\">").append("\r\n");
		int length = dataList.size();
		int count = 0;
		buffer.append("    ");
		for (Map<String, Object> map : dataList) {
			count++;
			buffer.append(map.get("columnName"));
			if (count != length) {
				buffer.append(", ");
			}
		}
		buffer.append("\r\n");
		buffer.append("</sql>").append("\r\n");

		// insert配置
		buffer.append("<insert id=\"insert\" parameterType=\"\">").append("\r\n");
		buffer.append("    insert into ").append(getTableName()).append("\r\n");
		buffer.append("    <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">").append("\r\n");
		for (Map<String, Object> map : dataList) {
			String columnName = (String) map.get("columnName");
			String fieldName = getFieldName(columnName);
			buffer.append("        <if test=\"").append(fieldName).append(" != null\"> \r\n");
			buffer.append("            ").append(columnName).append(",").append("\r\n");
			buffer.append("        </if> \r\n");
		}
		buffer.append("    </trim>").append("\r\n");
		buffer.append("    <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\"> \r\n");
		for (Map<String, Object> map : dataList) {
			String columnName = (String) map.get("columnName");
			String fieldName = getFieldName(columnName);
			String jdbcType = getMybatisJdbcType((Integer) map.get("dataType"));
			buffer.append("        <if test=\"").append(fieldName).append(" != null\"> \r\n");
			buffer.append("            #{").append(fieldName).append(",jdbcType=").append(jdbcType).append("}, \r\n");
			buffer.append("        </if> \r\n");
		}
		buffer.append("    </trim>").append("\r\n");

		// update配置
		buffer.append("<update id=\"update\" parameterType=\"java.util.Map\"> \r\n");
		buffer.append("    update ").append(getTableName()).append("\r\n");
		buffer.append("    <set>").append("\r\n");
		for (Map<String, Object> map : dataList) {
			String columnName = (String) map.get("columnName");
			String fieldName = getFieldName(columnName);
			String jdbcType = getMybatisJdbcType((Integer) map.get("dataType"));
			buffer.append("        <if test=\"").append(fieldName).append(" != null\"> \r\n");
			buffer.append("            ").append(columnName).append(" = ").append("#{").append(fieldName)
					.append(",jdbcType=").append(jdbcType).append("}, \r\n");
			buffer.append("        </if> \r\n");
		}
		buffer.append("    </set> \r\n");
		buffer.append("    where ").append("\r\n");

		// for (String primaryKey : primaryKeyList)
		// {
		// buffer.append(" ").append(primaryKey).append(" = #{lnNo,jdbcType=CHAR}");
		// }

		buffer.append("</update>");

		System.out.println(buffer.toString());
	}
*/

    public String getAddSql(String tableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"<script>\",\r\n");
        buffer.append("    \"insert into ").append(tableName).append("\",\r\n");
        buffer.append("    \"<trim prefix=\\\"(\\\" suffix=\\\")\\\" suffixOverrides=\\\",\\\">\",").append("\r\n");
        for (Map<String, Object> map : dataList) {
            String columnName = (String) map.get("columnName");
            String fieldName = getFieldName(columnName);
            buffer.append("        \"<if test=\\\"").append(fieldName).append(" != null\\\">\", \r\n");
            buffer.append("            \"").append(columnName).append(",").append("\",\r\n");
            buffer.append("        \"</if>\", \r\n");
        }
        buffer.append("    \"</trim>\",").append("\r\n");
        buffer.append("    \"<trim prefix=\\\"values (\\\" suffix=\\\")\\\" suffixOverrides=\\\",\\\">\", \r\n");
        for (Map<String, Object> map : dataList) {
            String columnName = (String) map.get("columnName");
            String fieldName = getFieldName(columnName);
            String jdbcType = getMybatisJdbcType((Integer) map.get("dataType"));
            buffer.append("        \"<if test=\\\"").append(fieldName).append(" != null\\\">\", \r\n");
            buffer.append("            \"#{").append(fieldName).append(",jdbcType=").append(jdbcType).append("}, \",\r\n");
            buffer.append("        \"</if>\", \r\n");
        }
        buffer.append("    \"</trim>\",").append("\r\n");
        buffer.append("\"</script>\" \r\n");
        return buffer.toString();
    }


    private String getSelectListSql(String tableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("     \"<script>\",\r\n");
        buffer.append("            \" select \",\r\n");

        int a = 0;
        int current_count = 0;
        for (Map<String, Object> map : dataList) {
            if (a == 0) {
                buffer.append("            \"");
            }
            String columnName = (String) map.get("columnName");
            if (columnName.indexOf("_") > 0) {
                columnName = columnName + " as " + Utils.getFirstLowerCaseAndSplitLine(columnName);
            }
            if (!columnName.contains("_") && columnName.substring(0, 1).toUpperCase().equals(columnName.substring(0, 1))) {
                columnName = columnName + " as " + getFieldName(columnName);
            }
            if (current_count == dataList.size() - 1) {
                buffer.append(" " + columnName);
            } else {
                buffer.append(" " + columnName + ",");
            }
            a++;
            current_count++;
            if (a == 5 || current_count == dataList.size()) {
                if (current_count == dataList.size()) {
                    buffer.append(" \",\r\n");
                } else {
                    buffer.append(" \",\r\n");
                    a = 0;
                }
            }

        }
        buffer.append("            \" from " + tableName + " \",\r\n" +
                "            \" <where>\",\r\n" +
                "                 \" <if test='belongToId!=null and belongToId !=\\\"\\\"'>\",\r\n" +
                "                 \" </if>\",\r\n" +
                "                 \" <if test='findKey!=null and findKey !=\\\"\\\"'>\",\r\n" +
                "                 \" </if>\",\r\n" +
                "            \" </where>\",\r\n" +
                "            \" order by id desc limit #{offset},#{pageCount} \",\r\n" +
                "        \"</script>\"\r\n");
        return buffer.toString();
    }

    private String getSelectCountSql(String tableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"<script>\",\r\n" +
                "		\"<bind name='pattern' value=\\\"'%'+findKey+'%'\\\" />\",\r\n" +
                "    	\"select count(*) \",\r\n" +
                "    	\"from " + tableName + " \",\r\n" +
                "    	\"<where>\",\r\n" +
                "    	\"<if test='belongToId!=null and belongToId !=\\\"\\\"'>\",\r\n" +
                "    	\"</if>\",\r\n" +
                "    	\"<if test='findKey!=null and findKey !=\\\"\\\"'>\",\r\n" +
                "    	\"</if>\",\r\n" +
                "    	\"</where>\",\r\n" +
                "    	\"</script>\"");
        return buffer.toString();
    }

    private String getSelectByIdSql(String tableName) {
        return getSelectByFiledSql(tableName, "id");
    }


    private String getSelectByFiledSql(String tableName, String filedS) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("     \"<script>\",\r\n");
        buffer.append("            \" select \",\r\n");

        int a = 0;
        int currentCount = 0;
        for (Map<String, Object> map : dataList) {
            if (a == 0) {
                buffer.append("            \"");
            }
            String columnName = (String) map.get("columnName");
            if (columnName.indexOf("_") > 0) {
                columnName = columnName + " as " + Utils.getFirstLowerCaseAndSplitLine(columnName);
            }
            if (!columnName.contains("_") && columnName.substring(0, 1).toUpperCase().equals(columnName.substring(0, 1))) {
                columnName = columnName + " as " + getFieldName(columnName);
            }
            if (currentCount == dataList.size() - 1) {
                if (columnName.indexOf("_") > 0) {
                    buffer.append(" " + columnName);
                } else {
                    buffer.append(" " + columnName);
                }
            } else {
                buffer.append(" " + columnName + ",");
            }
            a++;
            currentCount++;
            if (a == 5 || currentCount == dataList.size()) {
                buffer.append(" \",\r\n");
                a = 0;
            }
        }
        buffer.append("            \" from " + tableName + "\",\r\n" +
                "            \" <where>\",\r\n");
        buffer.append("            \"   " + getQueryParamInSqlWhenMultiFiled(filedS) + "\",\r\n");
        buffer.append("            \" </where>\",\r\n" +
                "        \"</script>\"\r\n");
        return buffer.toString();
    }

    /**
     * 获取查询参数 当有查询字段条件时
     *
     * @param columnNameS
     * @return
     */
    public String getQueryParamInSqlWhenMultiFiled(String columnNameS) {
        return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i))
                .map(x -> x + "=#{" + getFieldCataInSql(x) + "}").collect(Collectors.joining(" and "));
    }

    /**
     * 获取Dao方法上的查询参数 当有多个查询字段条件时
     *
     * @param columnNameS
     * @return
     */
    public String getFunctionParamInDaoFunctionWhenMultiFiled(String columnNameS) {
        return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i))
                .map(x -> "@Param(\"" + getFieldCataInSql(x) + "\") " + getJavaTypeByColumnName(x) + " " + getFieldCataInSql(x) + "").collect(Collectors.joining(" , "));
    }

    /**
     * 获取Controller方法上的查询参数 当有多个查询字段条件时
     *
     * @param columnNameS
     * @return
     */
    public String getFunctionParamInControllerFunctionWhenMultiFiled(String columnNameS) {
        return getFunctionParamWhenMultiFiled(columnNameS, "@RequestParam ", " , ");
    }

    public String getFunctionParamWhenMultiFiled(String columnNameS, String pattern, String split) {
        return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i))
                .map(x -> pattern + getJavaTypeByColumnName(x) + " " + getFieldCataInSql(x) + "").collect(Collectors.joining(split));
    }

    /**
     * 获取Controller方法上的查询参数 当有多个查询字段条件时
     *
     * @param columnNameS
     * @return
     */
    public String getSwaggerApiParamWhenMultiFiled(String columnNameS) {
        Stream<String> colunmNotNull = Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i));
        if (colunmNotNull.count() == 1) {
            return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i)).map(i -> "    @ApiImplicitParam(paramType=\"query\", name = \"" + getFieldCataInSql(i) + "\",value=\"" + tableComment + getFieldCataInSql(i) + getColumnComment(i) + "\",required=true,dataType=\"" + getJavaTypeByColumnName(i) + "\")\r\n").collect(Collectors.joining(","));
        } else {
            String temp = "    @ApiImplicitParams({\r\n";
            temp += Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i)).map(i -> "    @ApiImplicitParam(paramType=\"query\", name = \"" + getFieldCataInSql(i) + "\",value=\"" + tableComment + getFieldCataInSql(i) + getColumnComment(i) + "\",required=true,dataType=\"" + getJavaTypeByColumnName(i) + "\")\r\n").collect(Collectors.joining(","));
            temp += "    })\r\n";
            return temp;
        }
    }

    /**
     * 当有多个查询字段条件时,获取字段在方法注释上的拼接
     *
     * @param columnNameS
     * @return
     */
    public String getFiledInFunctionCommentWhenMultiFiled(String columnNameS) {
        if (columnNameS.contains(FILED_SPLIT)) {
            return Arrays.stream(columnNameS.split(FILED_SPLIT)).filter(i -> !Utils.isEmpty(i))
                    .map(x -> "	 * @param " + getFieldCataInSql(x) + " " + this.tableComment + getColumnComment(x)).collect(Collectors.joining("\r\n"));
        } else {
            return "	 * @param " + getFieldCataInSql(columnNameS) + " " + this.tableComment + getColumnComment(columnNameS) + "\r\n";
        }
    }

    private String getColumnComment(String columnName) {
        String comment = "";
        for (Map<String, Object> map : dataList) {
            String fieldName = (String) map.get("columnName");
            String remarks = (String) map.get("remarks");
            if (fieldName.equals(columnName)) {
                comment = remarks;
                break;
            }
        }
        return comment;
    }

    /**
     * 通过字段名获取java类型
     *
     * @param columnName
     * @return
     */
    public String getJavaTypeByColumnName(String columnName) {
        String javaType = "";
        for (Map<String, Object> map : dataList) {
            String fieldName = (String) map.get("columnName");
            if (fieldName.equals(columnName)) {
                javaType = getJavaType((Integer) map.get("dataType"));
                break;
            }
        }
        return javaType;
    }

    /**
     * 获取字段别名在sql语句中
     *
     * @param columnName
     * @return
     */
    public static String getFieldCataInSql(String columnName) {
        String result = columnName;
        if (columnName.indexOf("_") > 0) {
            result = Utils.getFirstLowerCaseAndSplitLine(columnName);
        }
        if (!columnName.contains("_") && columnName.substring(0, 1).toUpperCase().equals(columnName.substring(0, 1))) {
            result = getFieldName(columnName);
        }
        return result;
    }


    private String getEditSql(String tableName) {
        // update配置
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"<script>\",\r\n");
        buffer.append("    \"update ").append(tableName).append("\",\r\n");
        buffer.append("    \"<set>").append("\",\r\n");
        for (Map<String, Object> map : dataList) {
            String columnName = (String) map.get("columnName");
            String fieldName = getFieldName(columnName);
            String jdbcType = getMybatisJdbcType((Integer) map.get("dataType"));
            buffer.append("        \"<if test=\\\"").append(fieldName).append(" != null\\\">\", \r\n");
            buffer.append("            \"").append(columnName).append(" = ").append("#{").append(fieldName)
                    .append(",jdbcType=").append(jdbcType).append("},\", \r\n");
            buffer.append("        \"</if>\", \r\n");
        }
        buffer.append("    \"</set>\", \r\n");
        buffer.append("    \"where  id = #{id} \", ").append("\r\n");
        buffer.append("\"</script>\" \r\n");
        return buffer.toString();
    }

    private String getDeleteSql(String tableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"<script>\",\r\n" +
                "    	\"delete from " + tableName + " where id in \",\r\n" +
                "    	\"<foreach collection='list' item='item' open='(' separator=',' close=')'>\",\r\n" +
                "        \"#{item}\",\r\n" +
                "        \"</foreach>\",\r\n" +
                "    	\"</script>\" \r\n");
        return buffer.toString();
    }

    /**
     * 根据列的类型，获取mybatis配置中的jdbcType
     *
     * @param dataType 列的类型
     * @return String jdbcType
     */
    private static String getMybatisJdbcType(int dataType) {
        String jdbcType = "";
        if (dataType == Types.TINYINT) {
            jdbcType = "TINYINT";
        } else if (dataType == Types.SMALLINT) {
            jdbcType = "SMALLINT";
        } else if (dataType == Types.INTEGER) {
            jdbcType = "INTEGER";
        } else if (dataType == Types.BIGINT) {
            jdbcType = "BIGINT";
        } else if (dataType == Types.FLOAT) {
            jdbcType = "FLOAT";
        } else if (dataType == Types.DOUBLE) {
            jdbcType = "DOUBLE";
        } else if (dataType == Types.DECIMAL) {
            jdbcType = "DECIMAL";
        } else if (dataType == Types.NUMERIC) {
            jdbcType = "NUMERIC";
        } else if (dataType == Types.VARCHAR) {
            jdbcType = "VARCHAR";
        } else if (dataType == Types.NVARCHAR) {
            jdbcType = "NVARCHAR";
        } else if (dataType == Types.CHAR) {
            jdbcType = "CHAR";
        } else if (dataType == Types.NCHAR) {
            jdbcType = "NCHAR";
        } else if (dataType == Types.CLOB) {
            jdbcType = "CLOB";
        } else if (dataType == Types.BLOB) {
            jdbcType = "BLOB";
        } else if (dataType == Types.NCLOB) {
            jdbcType = "NCLOB";
        } else if (dataType == Types.DATE) {
            jdbcType = "DATE";
        } else if (dataType == Types.TIMESTAMP) {
            jdbcType = "TIMESTAMP";
        } else if (dataType == Types.ARRAY) {
            jdbcType = "ARRAY";
        } else if (dataType == Types.TIME) {
            jdbcType = "TIME";
        } else if (dataType == Types.BOOLEAN) {
            jdbcType = "BOOLEAN";
        } else if (dataType == Types.BIT) {
            jdbcType = "BIT";
        } else if (dataType == Types.BINARY) {
            jdbcType = "BINARY";
        } else if (dataType == Types.OTHER) {
            jdbcType = "OTHER";
        } else if (dataType == Types.REAL) {
            jdbcType = "REAL";
        } else if (dataType == Types.LONGVARCHAR) {
            jdbcType = "LONGVARCHAR";
        } else if (dataType == Types.VARBINARY) {
            jdbcType = "VARBINARY";
        } else if (dataType == Types.LONGVARBINARY) {
            jdbcType = "LONGVARBINARY";
        }

        return jdbcType;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getFiledFiledSFunctionName(String columnNameS) {
        return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i))
                .map(x -> firstUpperCase(getFieldName(x))).collect(Collectors.joining(" And "));
    }

    public String getFiledNameSInDoMain(String columnNameS) {
        return Arrays.stream(columnNameS.split(",")).filter(i -> !Utils.isEmpty(i)).map(i -> getFieldName(i)).collect(Collectors.joining(","));
    }

    /**
     * 通过数据库来得到所有表名
     */
    public List<String> getAllTableName() throws SQLException, ClassNotFoundException {
        List<String> tableNameS = null;
        Connection conn = null;
        try {
            conn = getConnection();
            tableNameS = new ArrayList<String>();
            DatabaseMetaData dbmd = conn.getMetaData();

            ResultSet rs = dbmd.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                // 获得表名
                String tableName = rs.getString("TABLE_NAME");
                tableNameS.add(tableName);
            }
        } finally {
            try {
                assert conn != null;
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableNameS;
    }

    /**
     * 根据连接获取数据库名
     */
    public String getDataBaseName(String url) {
        String beforeValue = url.substring(0, url.indexOf("?"));
        beforeValue = beforeValue.replace("//", "*");
        return beforeValue.substring(beforeValue.indexOf("/") + 1);
    }

    private boolean tableIsExists(String tableName) throws SQLException, ClassNotFoundException {
        //获取数据库名
        String dbUrl = dataSourceConfigPojo.getUrl();
        String dataBaseName = getDataBaseName(dbUrl);
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement pStemt = null;
            ResultSet rs = null;
            pStemt = (PreparedStatement) conn.prepareStatement("Select table_name,TABLE_COMMENT  from INFORMATION_SCHEMA.TABLES Where table_schema = '" + dataBaseName + "' and table_name ='" + tableName + "'");
            rs = pStemt.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取表的注释
     */
    public void getTableComment(String tableNameParam) throws SQLException, ClassNotFoundException {
        //获取数据库名
        String dbUrl = dataSourceConfigPojo.getUrl();
        String dataBaseName = getDataBaseName(dbUrl);
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement pStemt = null;
            ResultSet rs = null;
            pStemt = (PreparedStatement) conn.prepareStatement("Select table_name,TABLE_COMMENT  from INFORMATION_SCHEMA.TABLES Where table_schema = '" + dataBaseName + "' and table_name ='" + tableNameParam + "'");
            rs = pStemt.executeQuery();
            while (rs.next()) {
                String tableName = rs.getString(1);
                String tableComment = rs.getString(2);
                this.tableComment = tableComment;
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
    }
}
//