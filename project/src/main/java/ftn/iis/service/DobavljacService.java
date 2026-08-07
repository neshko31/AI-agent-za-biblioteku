package ftn.iis.service;

import ftn.iis.dto.DobavljacDetaljniDto;
import ftn.iis.dto.DobavljacDto;
import ftn.iis.dto.DobavljacIzmenaDto;
import ftn.iis.dto.OsnovniDobavljacDto;
import ftn.iis.enums.StatusDobavljaca;
import ftn.iis.enums.StatusUgovora;
import ftn.iis.exception.*;
import ftn.iis.model.Dobavljac;
import ftn.iis.model.Izdavac;
import ftn.iis.model.Knjizara;
import ftn.iis.model.Ugovor;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.repository.IzdavacRepository;
import ftn.iis.repository.KnjizaraRepository;
import ftn.iis.repository.UgovorRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DobavljacService {
    private final DobavljacRepository dobavljacRepository;
    private final JwtService jwtService;
    private final IzdavacRepository izdavacRepository;
    private final KnjizaraRepository knjizaraRepository;
    private final UgovorRepository ugovorRepository;

    public DobavljacService(DobavljacRepository dobavljacRepository, JwtService jwtService,
                            IzdavacRepository izdavacRepository, KnjizaraRepository knjizaraRepository,
                            UgovorRepository ugovorRepository){
        this.dobavljacRepository = dobavljacRepository;
        this.jwtService = jwtService;
        this.izdavacRepository = izdavacRepository;
        this.knjizaraRepository = knjizaraRepository;
        this.ugovorRepository = ugovorRepository;
    }

    @Transactional

    public DobavljacDetaljniDto kreirajDobavljaca(String token, DobavljacDto dto){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerCreatingSupplierException();
        }
        // Ako je ulogovan menadzer, sledi unos dobavljaca
        // 1. Provera preko PIB-a
        if (dobavljacRepository.existsByPib(dto.getPib())) {
            throw new SupplierPibAlreadyExists();
        }

        // 2. Provera preko Email-a
        if (dobavljacRepository.existsByEmail(dto.getEmail())) {
            throw new SupplierEmailAlreadyExists();
        }

        // 3. Provera preko naziva
        if (dobavljacRepository.existsByNaziv(dto.getNaziv())) {
            throw new SupplierNameAlreadyExists();
        }

        // 4. Provera preko telefona
        if (dobavljacRepository.existsByTel(dto.getTel())) {
            throw new SupplierPhoneAlreadyExists();
        }

        // Ako je sve u redu, mapiram i čuvam OSNOVNOG dobavljaca
        Dobavljac dobavljac = new Dobavljac(
                dto.getNaziv(),
                dto.getEmail(),
                dto.getTel(),
                dto.getPib()
        );

        dobavljac = dobavljacRepository.save(dobavljac);

        String url = null;
        String tip = dto.getTipDobavljaca();
        //"00" → nije ni knjižara ni izdavač
        //"01" → samo knjižara
        //"10" → samo izdavač
        //"11" → i knjižara i izdavač

        // 2. Drugi karakter = knjižara
        if (tip.charAt(1) == '1') {
            Knjizara knjizara = new Knjizara(dobavljac);
            knjizara.setUrlOnlineProdavnice(dto.getUrlOnlineProdavnice());
            knjizaraRepository.save(knjizara);
            url = dto.getUrlOnlineProdavnice();
        }

        // 3. Prvi karakter = izdavač
        if (tip.charAt(0) == '1') {
            Izdavac izdavac = new Izdavac(dobavljac);
            izdavacRepository.save(izdavac);
        }

        // Isti DTO kao za detaljan prikaz
        DobavljacDetaljniDto rezultat = new DobavljacDetaljniDto();
        rezultat.setId(dobavljac.getId());
        rezultat.setNaziv(dobavljac.getNaziv());
        rezultat.setEmail(dobavljac.getEmail());
        rezultat.setTel(dobavljac.getTel());
        rezultat.setPib(dobavljac.getPib());
        rezultat.setTipDobavljaca(tip);
        rezultat.setUrlOnlineProdavnice(url);
        rezultat.setStatus(dobavljac.getStatus());

        return rezultat;
    }


    public List<OsnovniDobavljacDto> ispisiSve(String token){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerViewingSupplierException();
        }

        // Ako je ulogovan menadzer, sledi ispis svih dobavljaca, osnovne info
        List<Dobavljac> detaljni = dobavljacRepository.findAll();
        List<OsnovniDobavljacDto> osnovni = new ArrayList<>();
        for (Dobavljac d : detaljni){
            osnovni.add( new OsnovniDobavljacDto(d.getId() , d.getNaziv(), d.getTel(),  d.getStatus()));
        }
        return osnovni;
    }

    public DobavljacDetaljniDto ispisiJednog(String token, Long id){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerViewingSupplierException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id).orElseThrow(() -> new NoSupplierFound());

        //proveravam jel dobavljac i knjizara/izdavac kako bih ispisala dodatna polja po potrebi
        boolean jeIzdavac = dobavljac.getIzdavac() != null;
        boolean jeKnjizara = dobavljac.getKnjizara() != null;

        String tip = (jeIzdavac ? "1" : "0") + (jeKnjizara ? "1" : "0");
        String url = jeKnjizara ? dobavljac.getKnjizara().getUrlOnlineProdavnice() : null;

        DobavljacDetaljniDto dto = new DobavljacDetaljniDto();
        dto.setId(dobavljac.getId());
        dto.setNaziv(dobavljac.getNaziv());
        dto.setEmail(dobavljac.getEmail());
        dto.setTel(dobavljac.getTel());
        dto.setPib(dobavljac.getPib());
        dto.setTipDobavljaca(tip);
        dto.setUrlOnlineProdavnice(url);
        dto.setStatus(dobavljac.getStatus());

        return dto;
    }

    @Transactional
    public DobavljacDetaljniDto izmeni(String token, Long id, DobavljacIzmenaDto dto){
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerUpdatingSupplierException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id).orElseThrow(() -> new NoSupplierFound());

        // Ažuriram samo polja koja nisu null i koja su razlicita od trenutnih
        // Obavezno unique constraint
        if (dto.getNaziv() != null && !dto.getNaziv().equals(dobavljac.getNaziv())) {
            if (dobavljacRepository.existsByNaziv(dto.getNaziv())) {
                throw new SupplierNameAlreadyExists();
            }
            dobavljac.setNaziv(dto.getNaziv());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(dobavljac.getEmail())) {
            if (dobavljacRepository.existsByEmail(dto.getEmail())) {
                throw new SupplierEmailAlreadyExists();
            }
            dobavljac.setEmail(dto.getEmail());
        }
        if (dto.getTel() != null && !dto.getTel().equals(dobavljac.getTel())) {
            if (dobavljacRepository.existsByTel(dto.getTel())) {
                throw new SupplierPhoneAlreadyExists();
            }
            dobavljac.setTel(dto.getTel());
        }
        if (dto.getPib() != null && !dto.getPib().equals(dobavljac.getPib())) {
            if (dobavljacRepository.existsByPib(dto.getPib())) {
                throw new SupplierPibAlreadyExists();
            }
            dobavljac.setPib(dto.getPib());
        }
        dobavljacRepository.save(dobavljac);

        // Ažuriram Url ako je on prosledjen a jeste u pitanju knjizara
        if(dto.getUrlOnlineProdavnice() != null && dobavljac.getKnjizara() != null){
            dobavljac.getKnjizara().setUrlOnlineProdavnice(dto.getUrlOnlineProdavnice());
            knjizaraRepository.save(dobavljac.getKnjizara());
        }

        // Sklapam response DTO
        boolean jeIzdavac = dobavljac.getIzdavac() != null;
        boolean jeKnjizara = dobavljac.getKnjizara() != null;
        String tip = (jeIzdavac ? "1" : "0") + (jeKnjizara ? "1" : "0");
        String url = jeKnjizara ? dobavljac.getKnjizara().getUrlOnlineProdavnice() : null;

        DobavljacDetaljniDto rezultat = new DobavljacDetaljniDto();
        rezultat.setId(dobavljac.getId());
        rezultat.setNaziv(dobavljac.getNaziv());
        rezultat.setEmail(dobavljac.getEmail());
        rezultat.setTel(dobavljac.getTel());
        rezultat.setPib(dobavljac.getPib());
        rezultat.setTipDobavljaca(tip);
        rezultat.setUrlOnlineProdavnice(url);
        rezultat.setStatus(dobavljac.getStatus());

        return rezultat;
    }

    @Transactional
    public void obrisi(String token, Long id) {
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            throw new NonManagerDeletingSupplierException();
        }

        Dobavljac dobavljac = dobavljacRepository.findById(id).orElseThrow(() -> new NoSupplierFound());

        // 1. Postavljam sve aktivne ugovore na RASKINUT
        List<Ugovor> ugovori = ugovorRepository.findAllByDobavljacId(id);
        for (Ugovor u : ugovori) {
            if (u.getStatus() == StatusUgovora.AKTIVAN) {
                u.setStatus(StatusUgovora.RASKINUT);
                ugovorRepository.save(u);
            }
        }

        // 2. Logicko brisanje dobavljaca
        dobavljac.setStatus(StatusDobavljaca.NEAKTIVAN);
        dobavljacRepository.save(dobavljac);
    }
}
