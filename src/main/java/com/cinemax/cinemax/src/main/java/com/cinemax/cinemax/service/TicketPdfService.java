package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.BookingSeatRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import java.util.List;

// Spring creates this service to handle business rules
@Service
public class TicketPdfService {

    private final BookingSeatRepository
            bookingSeatRepository;


    // Constructor injection supplies the dependencies this class needs
    public TicketPdfService(
            BookingSeatRepository bookingSeatRepository) {

        this.bookingSeatRepository =
                bookingSeatRepository;
    }


    // Build a downloadable PDF from the ticket and booking details
    public byte[] generateTicketPdf(
            Ticket ticket) {

        Booking booking =
                ticket.getBooking();

        ShowSchedule show =
                booking.getShowSchedule();


        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                    .findByBooking_Id(
                        booking.getId()
                    );


        // Build a comma-separated list of seat numbers
        StringBuilder seatText = new StringBuilder();

        for (int index = 0; index < bookingSeats.size(); index++) {
            BookingSeat bookingSeat = bookingSeats.get(index);

            if (index > 0) {
                seatText.append(", ");
            }

            seatText.append(bookingSeat.getSeat().getSeatCode());
        }

        String seatNames = seatText.toString();


        try (
            PDDocument document =
                    new PDDocument();

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage();

            document.addPage(page);


            PDType1Font titleFont =
                    new PDType1Font(
                        Standard14Fonts.FontName
                            .HELVETICA_BOLD
                    );


            PDType1Font normalFont =
                    new PDType1Font(
                        Standard14Fonts.FontName
                            .HELVETICA
                    );


            try (
                PDPageContentStream content =
                    new PDPageContentStream(
                        document,
                        page
                    )
            ) {

                content.beginText();

                content.setFont(
                    titleFont,
                    22
                );

                content.newLineAtOffset(
                    70,
                    730
                );

                content.showText(
                    "CineMax Movies"
                );


                content.setFont(
                    titleFont,
                    16
                );

                content.newLineAtOffset(
                    0,
                    -40
                );

                content.showText(
                    "Electronic Movie Ticket"
                );


                content.setFont(
                    normalFont,
                    12
                );


                writeLine(
                    content,
                    "Ticket: "
                    + ticket.getTicketNumber()
                );

                writeLine(
                    content,
                    "Booking: "
                    + booking
                        .getBookingReference()
                );

                writeLine(
                    content,
                    "Customer: "
                    + booking
                        .getUser()
                        .getFullName()
                );

                writeLine(
                    content,
                    "Movie: "
                    + show
                        .getMovie()
                        .getTitle()
                );

                writeLine(
                    content,
                    "Theatre: "
                    + show
                        .getScreen()
                        .getTheatre()
                        .getName()
                );

                writeLine(
                    content,
                    "Location: "
                    + show
                        .getScreen()
                        .getTheatre()
                        .getLocation()
                );

                writeLine(
                    content,
                    "Screen: "
                    + show
                        .getScreen()
                        .getName()
                );

                writeLine(
                    content,
                    "Date: "
                    + show.getShowDate()
                );

                writeLine(
                    content,
                    "Time: "
                    + show.getStartTime()
                );

                writeLine(
                    content,
                    "Seats: "
                    + seatNames
                );

                writeLine(
                    content,
                    "Amount Paid: Rs. "
                    + booking
                        .getTotalAmount()
                );


                content.newLineAtOffset(
                    0,
                    -30
                );

                content.setFont(
                    titleFont,
                    11
                );

                content.showText(
                    "Please present this ticket at the cinema entrance."
                );


                content.endText();
            }


            document.save(output);

            return output.toByteArray();


        } catch (Exception e) {

            throw new RuntimeException(
                "Could not generate ticket PDF",
                e
            );
        }
    }


    // Move down one line and write text into the PDF
    private void writeLine(
            PDPageContentStream content,
            String text)
            throws Exception {

        content.newLineAtOffset(
            0,
            -25
        );

        content.showText(text);
    }
}
