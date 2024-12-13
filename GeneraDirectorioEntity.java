import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class GeneraDirectorioEntity {

    public void GeneraDirectorioEntity(String directorioEntidad, String nombreEntidad, String entidadMayusculaInicial,
            Integer tipoSo, String nombreTablaValidada)
            throws SQLException, ClassNotFoundException, IOException {

        File directorioEntity = new File(directorioEntidad);

        if (directorioEntity.mkdir()) {
            System.out.println("   Directorio " + nombreEntidad + "/entity creado satisfactoriamente.");

            Utilitarios utilitarios = new Utilitarios();

            Properties config = new Properties();
            InputStream configInput = null;

            if (tipoSo == 0) {
                configInput = new FileInputStream("resources/config.properties");
            } else {
                configInput = new FileInputStream("C:\\config.properties");
            }

            config.load(configInput);

            String dataSourceUrl = config.getProperty("datasource.url");
            String dataSourceUsername = config.getProperty("datasource.username");
            String dataSourcePassword = config.getProperty("datasource.password");
            String schema = config.getProperty("schema");
            String paquete = config.getProperty("paquete");

            System.out.println("CREANDO EL ARCHIVO ENTIDAD... ");

            Connection conexion;
            Statement st;
            Statement st2;

            Class.forName("org.postgresql.Driver");

            conexion = DriverManager.getConnection(dataSourceUrl, dataSourceUsername, dataSourcePassword);

            st2 = conexion.createStatement();

            System.out.println("Obteniendo Informacion sobre una base de datos...");

            System.out.println("\nObteniendo Informacion sobre una consulta con un ResultSet...");

            System.out.println("SELECT * FROM information_schema.columns WHERE table_schema = '" + schema
                    + "' AND table_name = '" + nombreTablaValidada + "';");

            ResultSet datosCampos = st2.executeQuery(
                    "SELECT column_name, column_default, is_nullable,data_type, character_maximum_length FROM information_schema.columns WHERE table_schema = '"
                            + schema + "' AND table_name = '" + nombreTablaValidada + "';");

            // rsmetadatos = rs.getMetaData();

            st = conexion.createStatement();
            // int col = rsmetadatos.getColumnCount();

            ResultSet relaciones = st.executeQuery("SELECT k1.table_schema,\n" + //
                    "       k1.table_name,\n" + //
                    "       k1.column_name,\n" + //
                    "       k2.table_schema AS referenced_table_schema,\n" + //
                    "       k2.table_name AS referenced_table_name,\n" + //
                    "       k2.column_name AS referenced_column_name\n" + //
                    "FROM information_schema.key_column_usage k1\n" + //
                    "JOIN information_schema.referential_constraints fk USING (constraint_schema, constraint_name)\n" + //
                    "JOIN information_schema.key_column_usage k2\n" + //
                    "  ON k2.constraint_schema = fk.unique_constraint_schema\n" + //
                    " AND k2.constraint_name = fk.unique_constraint_name\n" + //
                    " AND k2.ordinal_position = k1.position_in_unique_constraint\n" + //
                    " where k1.table_name ='" + nombreTablaValidada + "';");

            String camelCaseColumname = null;
            String columnDefault = null;
            String isNullable = null;
            String dataType = null;
            String tamanoCampo= null;

            String camelCaseRelacionesCampo = null;
            String camelCaseRelacionesTabla = null;

            System.out.println("Filas: FALTA EL COUNT");

            // File archivo = new File(nombre+"/entity");

            File fileEntity = new File(directorioEntidad);
            utilitarios.deleteFile(fileEntity);

            // if (directorioEntity.mkdir()) {
            // System.out.println(" Directorio " + directorioEntity + " creado
            // satisfactoriamente.\n");
            // }

            if (fileEntity.mkdir()) {
                System.out.println("   Archivo " + fileEntity + " creado satisfactoriamente.\n");

                try (FileWriter fw = new FileWriter(
                        directorioEntidad + "/" + entidadMayusculaInicial + ".java",
                        true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {

                    out.println("package " + paquete + "." + nombreEntidad + ".entity;");
                    out.println("");
                    out.println("import jakarta.persistence.Column;");
                    out.println("import jakarta.persistence.Entity;");
                    out.println("import jakarta.persistence.Id;");
                    out.println("import jakarta.persistence.ManyToMany;");
                    out.println("import jakarta.persistence.OneToMany;");
                    out.println("import jakarta.persistence.Table;");
                    out.println("import lombok.AllArgsConstructor;");
                    out.println("import lombok.Getter;");
                    out.println("import lombok.NoArgsConstructor;");
                    out.println("import lombok.Setter;");
                    out.println("");
                    out.println("import java.util.Set;");
                    out.println("");
                    out.println("import com.fasterxml.jackson.annotation.JsonIgnore;");
                    out.println("");
                    out.println("@Entity");
                    out.println("@Table(schema = \"" + schema + "\", name = \"" + nombreTablaValidada + "\")");
                    out.println("@Getter");
                    out.println("@Setter");
                    out.println("@NoArgsConstructor");
                    out.println("@AllArgsConstructor");
                    out.println("");
                    out.println("public class " + entidadMayusculaInicial + " extends Base {");
                    out.println("");
                    out.println("   private static final long serialVersionUID = 1L;");
                    out.println("");
                    out.println("   @Id");
                    out.println("");

                    while (datosCampos.next()) {

                        isNullable = datosCampos.getString(3);
                        dataType = datosCampos.getString(4);
                        camelCaseColumname = utilitarios.camelCase(datosCampos.getString(1));
                        columnDefault = datosCampos.getString(2);
                        tamanoCampo = datosCampos.getString(5);
                       

                        // System.out.println("este es "+columnDefault);
                        if (dataType.equals("numeric")) {

                            dataType = "BigDecimal";

                            System.out.println("/* verificar que no sea un campo de relacion");
                            out.println("/* verificar que no sea un campo de relacion");

                            System.out.print("@Column");
                            out.print("@Column");
                            System.out.println("");
                            out.println("");

                            if (columnDefault != null) {
                                // System.out.println("este es el valor de dfault " +columnDefault);

                                // System.out.println("campo default " + columnDefault);
                                boolean empiezaConN = columnDefault.substring(0).startsWith("n");
                                if (empiezaConN) {
                                    System.out.println("private " + dataType + " " + camelCaseColumname + ";");
                                    out.println("private " + dataType + " " + camelCaseColumname + ";");
                                    System.out.println("");
                                    out.println("");
                                    
                                } else {
                                    String valorExtraidoDefault = utilitarios.extraerValorDefault(columnDefault);
                                    System.out.println("private " + dataType + " " + camelCaseColumname + " = \""
                                            + valorExtraidoDefault + "\";");
                                    out.println("private " + dataType + " " + camelCaseColumname + " = \""
                                            + valorExtraidoDefault + "\";");
                                }
                                System.out.println("");
                                out.println("");
                                

                            } else {
                                System.out.println("private " + dataType + " " + camelCaseColumname + ";");
                                out.println("private " + dataType + " " + camelCaseColumname + ";");
                                System.out.println("");
                                out.println("");
                                

                            }
                            System.out.println("*/");
                            out.println("*/");

                        } else {

                            System.out.print("@Column");
                            out.print("@Column");
                            if (isNullable.equals("YES")
                                    && (dataType.equals("date") || dataType.equals("timestamp without time zone"))) {
                               // System.out.print("(nullable = false)");
                            }

                            
                            if (dataType.equals("timestamp without time zone")) {
                                dataType = "OffsetDateTime";
                            }
                            if (dataType.equals("date")) {
                                dataType = "LocalDate";
                            }
                            if (dataType.equals("character varying")) {
                                dataType = "String";
                            }
                            if (dataType.equals("integer")) {
                                dataType = "Long";
                            }

                            if(tamanoCampo == null){
                                // no hgas nada
                            }
                                else {
                                    System.out.print("(length = "+ tamanoCampo + ")");
                                    out.print("(length = "+ tamanoCampo + ")");
                                }
                            

                            System.out.println("");
                            out.println("");

                            if (columnDefault != null) {
                                // System.out.println("este es el valor de dfault " +columnDefault);

                                // System.out.println("campo default " + columnDefault);
                                boolean empiezaConN = columnDefault.substring(0).startsWith("n");
                                if (empiezaConN) {
                                    System.out.println("private " + dataType + " " + camelCaseColumname + ";");
                                    out.println("private " + dataType + " " + camelCaseColumname + ";");
                                } else {
                                    String valorExtraidoDefault = utilitarios.extraerValorDefault(columnDefault);
                                    System.out.println("private " + dataType + " " + camelCaseColumname + " = \""
                                            + valorExtraidoDefault + "\";");
                                    out.println("private " + dataType + " " + camelCaseColumname + " = \""
                                            + valorExtraidoDefault + "\";");
                                }

                            } else {
                                System.out.println("private " + dataType + " " + camelCaseColumname + ";");
                                out.println("private " + dataType + " " + camelCaseColumname + ";");

                            }

                            System.out.println("");
                            out.println("");

                        }

                    }

                    /*
                     * for (int i = 1; i <= col; i++) {
                     * 
                     * String tipoJava =
                     * utilitarios.generaTipoJava(rsmetadatos.getColumnClassName(i));
                     * 
                     * String nombreCamelcase = utilitarios.camelCase(rsmetadatos.getColumnName(i));
                     * 
                     * String nombreCamelcase2 =
                     * utilitarios.camelCase(rsmetadatos.getColumnName(i));
                     * 
                     * 
                     * 
                     * System.out.print("@Column");
                     * if (rsmetadatos.isNullable(i) == 0 && tipoJava.equals("Date") &&
                     * tipoJava.equals("Timestamp")) {
                     * System.out.println("(nullable = false)");
                     * }
                     * if (rsmetadatos.isNullable(i) == 0
                     * && (!tipoJava.equals("Date") && !tipoJava.equals("Timestamp"))) {
                     * System.out.println(
                     * "(nullable = false, length = " + rsmetadatos.getColumnDisplaySize(i) + ")");
                     * }
                     * if (rsmetadatos.isNullable(i) == 1
                     * && (!tipoJava.equals("Date") && !tipoJava.equals("Timestamp"))) {
                     * System.out.println("(length = " + rsmetadatos.getColumnDisplaySize(i) + ")");
                     * 
                     * }
                     * if (rsmetadatos.isNullable(i) == 1
                     * && (tipoJava.equals("Date") && tipoJava.equals("Timestamp"))) {
                     * System.out.println("\n");
                     * }
                     * 
                     * System.out.println("private " + tipoJava + " " + nombreCamelcase + ";");
                     * 
                     * out.print("@Column");
                     * 
                     * System.out.println("");
                     * if (rsmetadatos.isNullable(i) == 0 && tipoJava.equals("Date") &&
                     * tipoJava.equals("Timestamp")) {
                     * out.println("(nullable = false)");
                     * }
                     * if (rsmetadatos.isNullable(i) == 0
                     * && (!tipoJava.equals("Date") && !tipoJava.equals("Timestamp"))) {
                     * out.println("(nullable = false, length = " +
                     * rsmetadatos.getColumnDisplaySize(i) + ")");
                     * }
                     * if (rsmetadatos.isNullable(i) == 1
                     * && (!tipoJava.equals("Date") && !tipoJava.equals("Timestamp"))) {
                     * out.println("(length = " + rsmetadatos.getColumnDisplaySize(i) + ")");
                     * 
                     * }
                     * if (rsmetadatos.isNullable(i) == 1
                     * && (tipoJava.equals("Date") || tipoJava.equals("Timestamp"))) {
                     * out.println("");
                     * }
                     * 
                     * out.println("private " + tipoJava + " " + nombreCamelcase + ";");
                     * 
                     * out.println("");
                     * }
                     */

                    while (relaciones.next()) {

                        camelCaseRelacionesCampo = utilitarios.camelCase(relaciones.getString(3));
                        camelCaseRelacionesTabla = utilitarios.camelCase(relaciones.getString(5));

                        System.out.println("@ManyToOne(fetch = FetchType.LAZY)");
                        out.println("@ManyToOne(fetch = FetchType.LAZY)");
                        System.out.println("@JoinColumn(name = " + relaciones.getString(3) + ", nullable = false)");
                        out.println("@JoinColumn(name = \"" + relaciones.getString(3) + "\", nullable = false)");

                        System.out
                                .println("private " + utilitarios.generaMayusculaInicial(camelCaseRelacionesTabla) + " "
                                        + camelCaseRelacionesCampo + ";");

                        out.println("private " + utilitarios.generaMayusculaInicial(camelCaseRelacionesTabla) + " "
                                + camelCaseRelacionesCampo + ";");
                        System.out.println("");
                        out.println("");

                    }
                    out.println("}");

                } catch (IOException e) {
                    // exception handling left as an exercise for the reader
                }
            } else {
                System.out.println("no entro");
            }

        }

    }
}
