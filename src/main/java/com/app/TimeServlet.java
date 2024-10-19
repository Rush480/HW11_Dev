package com.app;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;


import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init() {
        engine = new TemplateEngine();
        initTemplateResolver();
    }

    private void initTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("./templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        String timezoneParam = getTimezoneParam(req, resp);

        String formattedTime = formatTimeWithTimeZone(timezoneParam);
        String htmlContent = getHtmlContent(formattedTime);

        resp.getWriter().write(htmlContent);

    }

    private String getTimezoneParam(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String timezoneParam = req.getParameter("timezone");
        if (timezoneParam != null) {
            addLastTimezoneCookie(resp, timezoneParam);
        } else {
            timezoneParam = getLastTimezoneFromCookies(req.getCookies());
        }
        return timezoneParam;
    }

    private void addLastTimezoneCookie(HttpServletResponse resp, String timezoneParam) throws IOException {
        Cookie lastTimezoneCookie = new Cookie("lastTimezone", URLEncoder.encode(timezoneParam, "UTF-8"));
        resp.addCookie(lastTimezoneCookie);
    }

    private String getLastTimezoneFromCookies(Cookie[] cookies) throws IOException {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("lastTimezone".equals(cookie.getName())) {
                return URLDecoder.decode(cookie.getValue(), "UTF-8");
            }
        }
        return null;
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

    private String getHtmlContent(String formattedTime) {
        Context context = new Context();
        context.setVariable("time", formattedTime);
        return engine.process("timeTemplate", context);
    }
}
