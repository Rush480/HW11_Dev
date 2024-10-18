package com.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;


import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String timezoneParam = req.getParameter("timezone");
        String formattedTime = formatTimeWithTimeZone(timezoneParam);


        String htmlContent = getHtmlContent(formattedTime);

        resp.getWriter().write(htmlContent);
        resp.getWriter().close();
    }

    private String getHtmlContent(String formattedTime) {
        Context context = new Context();
        context.setVariable("time", formattedTime);
        return engine.process("timeTemplate", context);
    }

    private String formatTimeWithTimeZone(String timezoneParam) {
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneId.of("UTC"));
        String timezone = "UTC";
        if (timezoneParam != null) {
            int offsetHours = Integer.parseInt(timezoneParam.substring(3).trim());
            currentTime = OffsetDateTime.now(ZoneOffset.ofHours(offsetHours));
            timezone = timezoneParam.replace(" ", "+");
        }
        return currentTime.format(FORMATTER) + " " + timezone;
    }

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("./templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }
}
