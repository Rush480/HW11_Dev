package com.app;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@WebFilter(filterName = "TimezoneValidateFilter", urlPatterns = {"/time"})
public class TimezoneValidateFilter extends HttpFilter {

    private static final Set<Integer> VALID_OFFSETS = new HashSet<>();

    static {
        for (int offset = -12; offset <= 14; offset++) {
            VALID_OFFSETS.add(offset);
        }
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        //String timezone = req.getParameter("timezone");
        String timezoneParam = getTimezoneParameter(req, resp);
//        if (timezone != null) {
//            Cookie cookie = new Cookie("lastTimezone", timezone);
//            resp.addCookie(cookie);
//            timezoneParam = timezone;
//        } else {
//            Cookie[] cookies = req.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if (cookie.getName().equals("lastTimezone")) {
//                        timezoneParam = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//        }
//        if (timezoneParam == null) {
//            chain.doFilter(req, resp);
//            return;
//        }
        if (timezoneParam.length() < 3) {
            sendError(resp, "Invalid or missing timezone parameter");
            return;
        }

        try {
            int offset = Integer.parseInt(timezoneParam.substring(3).trim());
            if (VALID_OFFSETS.contains(offset)) {
                chain.doFilter(req, resp);
            } else {
                sendError(resp, "Invalid timezone offset");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "Invalid timezone format");
        }
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(SC_BAD_REQUEST);
        resp.setContentType("text/html");
        resp.getWriter().write(message);
    }


    @Override
    public void destroy() {
        VALID_OFFSETS.clear();
        super.destroy();
    }

    private String getTimezoneParameter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String timezoneParameter = req.getParameter("timezone");
        if (timezoneParameter != null) {
            String encodedTimezone = URLEncoder.encode(timezoneParameter, StandardCharsets.UTF_8.toString());
            Cookie cookie = new Cookie("lastTimezone", encodedTimezone);
            resp.addCookie(cookie);
            return timezoneParameter;
        } else {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("lastTimezone".equals(cookie.getName())) {
                        return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.toString());
                    }
                }
            }
        }
        return null;
    }
}
