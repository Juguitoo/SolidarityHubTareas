package solidarityhub.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import solidarityhub.backend.model.PDFCertificate;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.repository.PDFCertificateRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Service
public class PDFCertificateService {
    private final PDFCertificateRepository documentoPDFRepository;
    public PDFCertificateService(PDFCertificateRepository documentoPDFRepository) {
        this.documentoPDFRepository = documentoPDFRepository;
    }

    public void createPDFDocument(Volunteer volunteer, Task task) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Cargar imagen (logo)
                InputStream imageStream = getClass().getResourceAsStream("/static/logo.png");
                if (imageStream == null) {
                    throw new IOException("No se encontró el recurso /static/logo.png");
                }
                byte[] imageBytes = imageStream.readAllBytes();
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo.png");
                contentStream.drawImage(logo, 50, 750, 40, 40);

                // Título principal
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(120, 760);
                contentStream.showText("Certificado de realización de tareas");
                contentStream.endText();

                // Escribir párrafo descriptivo
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Se deja constancia de que el/la voluntario/a con los siguientes datos:");
                contentStream.endText();

                // Dibujar tabla para datos de la persona
                float tableX = 50;
                float tableY = 690;
                float tableWidth = 500;
                float rowHeight = 20;
                int cols = 2;
                float[] colWidths = {150, 350};
                int numRows = 5;

                // Dibujar filas horizontales
                for (int i = 0; i <= numRows; i++) {
                    contentStream.moveTo(tableX, tableY - i * rowHeight);
                    contentStream.lineTo(tableX + tableWidth, tableY - i * rowHeight);
                }
                // Dibujar columnas verticales
                float nextX = tableX;
                for (int i = 0; i < cols; i++) {
                    contentStream.moveTo(nextX, tableY);
                    contentStream.lineTo(nextX, tableY - rowHeight * numRows);
                    nextX += colWidths[i];
                }
                // Línea final derecha
                contentStream.moveTo(tableX + tableWidth, tableY);
                contentStream.lineTo(tableX + tableWidth, tableY - rowHeight * numRows);
                contentStream.stroke();

                // Escribir contenido en la tabla: Datos de la persona
                float textX = tableX + 5;
                float textY = tableY - 15;
                String[][] personData = {
                        {"Nombre completo", volunteer.getFirstName() + " " + volunteer.getLastName()},
                        {"DNI/NIE", volunteer.getDni()},
                        {"Correo electrónico", volunteer.getEmail()},
                        {"Teléfono", volunteer.getPhone() + ""},
                        {"Dirección", volunteer.getHomeAddress()},
                };

                for (int i = 0; i < personData.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(textX, textY - i * rowHeight);
                    contentStream.showText(personData[i][0]);
                    contentStream.newLineAtOffset(colWidths[0], 0);
                    contentStream.showText(personData[i][1]);
                    contentStream.endText();
                }

                // Segunda sección: Tarea
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableY - rowHeight * numRows - 40);
                contentStream.showText("Ha colaborado activamente en la ejecución de la siguiente tarea solidaria:");
                contentStream.endText();

                // Dibujar tabla para datos de la tarea
                float tableYTask = tableY - rowHeight * numRows - 50;
                numRows = 6;
                // Dibujar filas horizontales
                for (int i = 0; i <= numRows; i++) {
                    contentStream.moveTo(tableX, tableYTask - i * rowHeight);
                    contentStream.lineTo(tableX + tableWidth, tableYTask - i * rowHeight);
                }
                // Dibujar columnas verticales
                nextX = tableX;
                for (int i = 0; i < cols; i++) {
                    contentStream.moveTo(nextX, tableYTask);
                    contentStream.lineTo(nextX, tableYTask - rowHeight * numRows);
                    nextX += colWidths[i];
                }
                contentStream.moveTo(tableX + tableWidth, tableYTask);
                contentStream.lineTo(tableX + tableWidth, tableYTask - rowHeight * numRows);
                contentStream.stroke();

                // Escribir contenido en la tabla de la tarea
                textX = tableX + 5;
                textY = tableYTask - 15;

                String[][] taskData = {
                        {"Título", task.getTaskName()},
                        {"Descripción", task.getTaskDescription()},
                        {"Peligrosidad", task.getEmergencyLevel().toString()},
                        {"Urgencia", task.getPriority().toString()},
                        {"Fecha de inicio", task.getStartTimeDate().toLocalDate().toString()},
                        {"Fecha de finalización", task.getEstimatedEndTimeDate().toString()},
                };
                for (int i = 0; i < taskData.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(textX, textY - i * rowHeight);
                    contentStream.showText(taskData[i][0]);
                    contentStream.newLineAtOffset(colWidths[0], 0);
                    contentStream.showText(taskData[i][1]);
                    contentStream.endText();
                }

                // Firma e información final
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableYTask - rowHeight * numRows - 40);
                contentStream.showText("Su participación ha sido fundamental para el cumplimiento de los objetivos de la actividad");
                contentStream.newLine();
                contentStream.showText("y ha contribuido significativamente al bienestar de la comunidad.");
                contentStream.newLine();
                contentStream.showText("");
                contentStream.newLine();
                contentStream.showText("Desde SolidarityHub agradecemos y valoramos su dedicación, esfuerzo y compromiso en");
                contentStream.newLine();
                contentStream.showText("la realización de esta tarea.");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableYTask - rowHeight * numRows - 150);
                contentStream.showText("Valencia, " + LocalDate.now().toString().replace("-", "/"));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                contentStream.newLineAtOffset(400, 150);
                contentStream.showText("Firma: SolidarityHub");
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            PDFCertificate certificate = new PDFCertificate("certificate_" + volunteer.getDni() + "_" + task.getId() + ".pdf", volunteer, task,  outputStream.toByteArray());
            documentoPDFRepository.save(certificate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGenericPDFDocument() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Cargar imagen (logo)
                InputStream imageStream = getClass().getResourceAsStream("/static/logo.png");
                if (imageStream == null) {
                    throw new IOException("No se encontró el recurso /static/logo.png");
                }
                byte[] imageBytes = imageStream.readAllBytes();
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo.png");
                contentStream.drawImage(logo, 50, 750, 40, 40);

                // Título principal
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(120, 760);
                contentStream.showText("Certificado de realización de tareas");
                contentStream.endText();

                // Escribir párrafo descriptivo
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Se deja constancia de que el/la voluntario/a con los siguientes datos:");
                contentStream.endText();

                // Dibujar tabla para datos de la persona
                float tableX = 50;
                float tableY = 690;
                float tableWidth = 500;
                float rowHeight = 20;
                int cols = 2;
                float[] colWidths = {150, 350};
                int numRows = 5;

                // Dibujar filas horizontales
                for (int i = 0; i <= numRows; i++) {
                    contentStream.moveTo(tableX, tableY - i * rowHeight);
                    contentStream.lineTo(tableX + tableWidth, tableY - i * rowHeight);
                }
                // Dibujar columnas verticales
                float nextX = tableX;
                for (int i = 0; i < cols; i++) {
                    contentStream.moveTo(nextX, tableY);
                    contentStream.lineTo(nextX, tableY - rowHeight * numRows);
                    nextX += colWidths[i];
                }
                // Línea final derecha
                contentStream.moveTo(tableX + tableWidth, tableY);
                contentStream.lineTo(tableX + tableWidth, tableY - rowHeight * numRows);
                contentStream.stroke();

                // Escribir contenido en la tabla: Datos de la persona
                float textX = tableX + 5;
                float textY = tableY - 15;
                String[][] personData = {
                        {"Nombre completo", "Hugo"},
                        {"DNI/NIE", "12345678A"},
                        {"Correo electrónico", "@correo"},
                        {"Teléfono",  "123455678"},
                        {"Dirección", "Mi casa"},
                };

                for (int i = 0; i < personData.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(textX, textY - i * rowHeight);
                    contentStream.showText(personData[i][0]);
                    contentStream.newLineAtOffset(colWidths[0], 0);
                    contentStream.showText(personData[i][1]);
                    contentStream.endText();
                }

                // Segunda sección: Tarea
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableY - rowHeight * numRows - 40);
                contentStream.showText("Ha colaborado activamente en la ejecución de la siguiente tarea solidaria:");
                contentStream.endText();

                // Dibujar tabla para datos de la tarea
                float tableYTask = tableY - rowHeight * numRows - 50;
                numRows = 6;
                // Dibujar filas horizontales
                for (int i = 0; i <= numRows; i++) {
                    contentStream.moveTo(tableX, tableYTask - i * rowHeight);
                    contentStream.lineTo(tableX + tableWidth, tableYTask - i * rowHeight);
                }
                // Dibujar columnas verticales
                nextX = tableX;
                for (int i = 0; i < cols; i++) {
                    contentStream.moveTo(nextX, tableYTask);
                    contentStream.lineTo(nextX, tableYTask - rowHeight * numRows);
                    nextX += colWidths[i];
                }
                contentStream.moveTo(tableX + tableWidth, tableYTask);
                contentStream.lineTo(tableX + tableWidth, tableYTask - rowHeight * numRows);
                contentStream.stroke();

                // Escribir contenido en la tabla de la tarea
                textX = tableX + 5;
                textY = tableYTask - 15;

                String[][] taskData = {
                        {"Título", "tarea"},
                        {"Descripción", "descripción"},
                        {"Peligrosidad", "Alto"},
                        {"Urgencia", "Alto"},
                        {"Fecha de inicio", "Inicio"},
                        {"Fecha de finalización", "Fin"},
                };
                for (int i = 0; i < taskData.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(textX, textY - i * rowHeight);
                    contentStream.showText(taskData[i][0]);
                    contentStream.newLineAtOffset(colWidths[0], 0);
                    contentStream.showText(taskData[i][1]);
                    contentStream.endText();
                }

                // Firma e información final
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableYTask - rowHeight * numRows - 40);
                contentStream.showText("Su participación ha sido fundamental para el cumplimiento de los objetivos de la actividad");
                contentStream.newLine();
                contentStream.showText("y ha contribuido significativamente al bienestar de la comunidad.");
                contentStream.newLine();
                contentStream.showText("");
                contentStream.newLine();
                contentStream.showText("Desde SolidarityHub agradecemos y valoramos su dedicación, esfuerzo y compromiso en");
                contentStream.newLine();
                contentStream.showText("la realización de esta tarea.");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, tableYTask - rowHeight * numRows - 150);
                contentStream.showText("Valencia, " + LocalDate.now().toString().replace("-", "/"));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                contentStream.newLineAtOffset(400, 150);
                contentStream.showText("Firma: SolidarityHub");
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            PDFCertificate certificate = new PDFCertificate("certificate.pdf", outputStream.toByteArray());
            documentoPDFRepository.save(certificate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
