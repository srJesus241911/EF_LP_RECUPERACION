package cibertec.com.pe.controller;

import cibertec.com.pe.model.Tique;
import cibertec.com.pe.model.Ciudad;
import cibertec.com.pe.model.Venta;
import cibertec.com.pe.model.VentaDetalle;
import cibertec.com.pe.repository.ICiudadRepository;
import cibertec.com.pe.repository.IVentaDetalleRepository;
import cibertec.com.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@SessionAttributes({"boletosAgregados"})
public class TiqueController {

    @Autowired
    private ICiudadRepository ciudadRepository;

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    @GetMapping("/index")
    public String inicioSlash(Model model) {
        try {
            List<Ciudad> ciudades = ciudadRepository.findAll();
            List<Tique> tiques = (List<Tique>) model.getAttribute("boletosAgregados");

            Tique boleto = tiques.isEmpty() ? new Tique() : tiques.get(tiques.size() - 1);

            model.addAttribute("boleto", boleto);
            model.addAttribute("ciudades", ciudades);
        } catch (Exception e) {
            // Handle exceptions gracefully
            // Log the error or perform appropriate actions
            e.printStackTrace();
            // Return an error page or redirect to a different URL
            return "error";
        }
        return "index";
    }

    @GetMapping("/volver-compra")
    public String volverCompra(Model model) {
        try {
            List<Ciudad> ciudades = ciudadRepository.findAll();

            model.addAttribute("boleto", new Tique());
            model.addAttribute("ciudades", ciudades);
            model.addAttribute("boletosAgregados", new ArrayList<>());
        } catch (Exception e) {
            // Handle exceptions gracefully
            // Log the error or perform appropriate actions
            e.printStackTrace();
            // Return an error page or redirect to a different URL
            return "error";
        }
        return "index";
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        try {
            List<Ciudad> ciudades = ciudadRepository.findAll();
            List<Tique> tiques = (List<Tique>) model.getAttribute("boletosAgregados");

            Tique boleto = tiques.isEmpty() ? new Tique() : tiques.get(tiques.size() - 1);

            model.addAttribute("boleto", boleto);
            model.addAttribute("ciudades", ciudades);
        } catch (Exception e) {
            // Handle exceptions gracefully
            // Log the error or perform appropriate actions
            e.printStackTrace();
            // Return an error page or redirect to a different URL
            return "error";
        }
        return "index";
    }

    @PostMapping("/agregar-boleto")
    public String agregarBoleto(Model model, @ModelAttribute Tique tique) {
        try {
            List<Ciudad> ciudades = ciudadRepository.findAll();
            List<Tique> tiques = (List<Tique>) model.getAttribute("boletosAgregados");
            Double precioBoleto = 50.00;

            tique.setSubTotal(tique.getCantidad() * precioBoleto);

            tiques.add(tique);

            model.addAttribute("boletosAgregados", tiques);
            model.addAttribute("ciudades", ciudades);
            model.addAttribute("boleto", new Tique());
        } catch (Exception e) {
            // Handle exceptions gracefully
            // Log the error or perform appropriate actions
            e.printStackTrace();
            // Return an error page or redirect to a different URL
            return "error";
        }
        return "redirect:/index";
    }

    @GetMapping("/comprar")
    public String comprar(Model model) {
        try {
            List<Tique> tiques = (List<Tique>) model.getAttribute("boletosAgregados");
            if (tiques.isEmpty()) {
                // Handle case where no tickets are added
                return "redirect:/index";
            }

            Double montoTotal = tiques.stream()
                                      .mapToDouble(Tique::getSubTotal)
                                      .sum();

            Venta nuevaVenta = new Venta();
            nuevaVenta.setFechaVenta(new Date());
            nuevaVenta.setMontoTotal(montoTotal);
            nuevaVenta.setNombreComprador(tiques.get(0).getNombreComprador());

            Venta ventaGuardada = ventaRepository.save(nuevaVenta);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            for (Tique tique : tiques) {
                VentaDetalle ventaDetalle = new VentaDetalle();
                Ciudad ciudadDestino = ciudadRepository.findById(tique.getCiudadDestino()).orElse(null);
                Ciudad ciudadOrigen = ciudadRepository.findById(tique.getCiudadOrigen()).orElse(null);

                if (ciudadDestino != null && ciudadOrigen != null) {
                    ventaDetalle.setCiudadDestino(ciudadDestino);
                    ventaDetalle.setCiudadOrigen(ciudadOrigen);
                    ventaDetalle.setCantidad(tique.getCantidad());
                    ventaDetalle.setSubTotal(tique.getSubTotal());

                    Date fechaRetorno = formatter.parse(tique.getFechaRetorno());
                    ventaDetalle.setFechaRetorno(fechaRetorno);

                    Date fechaSalida = formatter.parse(tique.getFechaSalida());
                    ventaDetalle.setFechaViaje(fechaSalida);

                    ventaDetalle.setVenta(ventaGuardada);

                    ventaDetalleRepository.save(ventaDetalle);
                }
            }

            // Clear the list of tickets after purchase
            tiques.clear();
            model.addAttribute("boletosAgregados", tiques);
        } catch (Exception e) {
            // Handle exceptions gracefully
            // Log the error or perform appropriate actions
            e.printStackTrace();
            // Return an error page or redirect to a different URL
            return "error";
        }
        return "confirmar";
    }

    @GetMapping("/limpiar")
    public String limpiar(Model model){
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Tique());
        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @ModelAttribute("boletosAgregados")
    public List<Tique> boletosComprados() {
        return new ArrayList<>();
    }
}

