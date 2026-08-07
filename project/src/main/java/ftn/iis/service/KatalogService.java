package ftn.iis.service;

import ftn.iis.dto.KatalogDto;
import ftn.iis.model.Katalog;
import ftn.iis.repository.KatalogRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KatalogService {
    private final KatalogRepository katalogRepository;

    public KatalogService(KatalogRepository katalogRepository) {
        this.katalogRepository = katalogRepository;
    }

    public boolean addNewCatalog(Katalog katalog){
        try{
            katalogRepository.saveAndFlush(katalog);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public List<Katalog> getAll(){

        List<Katalog> retKats = new ArrayList<>();
        List<Katalog> kats = katalogRepository.findAll();

        for(Katalog k: kats){
            if(!k.isDeleted()){
                retKats.add(k);
            }
        }

        return retKats;
    }

    public Optional<Katalog> getByKatId(Long katId){
        Optional<Katalog> k = katalogRepository.findByKatId(katId);
        if(k.isPresent() && !k.get().isDeleted())
            return k;
        return null;
    }

    public boolean deleteCatlog(Long katId){
        try {
            Optional<Katalog> k = katalogRepository.findByKatId(katId);
            if (k.isPresent()) {
                Katalog kat = k.get();
                kat.setDeleted(true);
                katalogRepository.saveAndFlush(kat);
            }
            return true;
        }
        catch (RuntimeException e){
            return false;
        }
    }

    public KatalogDto updateCatalog(KatalogDto katalogDto, Long katId){
        Optional<Katalog> kat = katalogRepository.findByKatId(katId);

        if(!kat.isPresent() || kat.get().isDeleted())
            return null;

        Katalog k = kat.get();
        k.setKatIme(katalogDto.getNaziv());
        k.setStandard(katalogDto.getStandard());
        katalogRepository.saveAndFlush(k);

        return katalogDto;
    }
}
