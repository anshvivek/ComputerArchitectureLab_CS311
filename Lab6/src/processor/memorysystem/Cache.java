package processor.memorysystem;
import generic.*;
import processor.*;

import configuration.Configuration;

//write through cache
public class Cache implements Element{
    boolean available = true ;
    public int latency ;
    Processor containingProcessor ;
    int cache_size ;
    int miss_address ;
    int line_size ;
    CacheLine[] cach ;
    int[] index ;
    //index- for the cache line to find
    //tag- find the element in that cache line

    public Cache(Processor proc , int lat , int cSize){

        int f = cSize/8;

        this.latency = lat ;
        this.cache_size = cSize;
        //line size is the number of bits available for index
        this.line_size = (int)(Math.log(f)/Math.log(2));
        this.containingProcessor = proc ;
        this.cach = new CacheLine[f];
        for(int i=0 ; i < f ; i++){
            this.cach[i] = new CacheLine();
        }
    }
        
    private int[] getindextag(int addr){
        // Convert the memory address to a binary string representation.
        String a = Integer.toBinaryString(addr);

        // Pad the binary string with leading zeros to ensure it's 32 bits long.
        String pad = "";
        for (int i = 0; i < 32 - a.length(); i++)
            pad = "0" + pad;
        a = pad + a ;

        // Extract the "tag" bits by taking the most significant bits.
        int add_tag = Integer.parseInt(a.substring(0, a.length() - line_size), 2);

        // Create a bit mask for extracting the "index" bits.
        //ind refers to the offset in a cache block
        int temp_ind;
        String ind = "0";
        if (line_size != 0){
            for(int i = 0; i < line_size; i++){
                ind = ind + "1";
            }
            //stores the index offset without padding
            //this is to extract the offset and mask off the remaining values.
            temp_ind = addr & Integer.parseInt(ind, 2);
        }
        
        else{
            temp_ind = 0;
        }
        return new int[]{temp_ind, add_tag};
    }

    public int cacheRead(int adr){
        int index = getindextag(adr)[0] ;
        int tag = getindextag(adr)[1];
        
        
        if(tag == cach[index].tag[1]){
            // cach[index].least_recently_used = 0;
            cach[index].setleastrecentlyused(0);
            available = true ;
            return cach[index].getData(1);
        }
        else if(tag == cach[index].tag[0]){
            // cach[index].least_recently_used = 1;
            cach[index].setleastrecentlyused(1);
            available = true ;
            return cach[index].getData(0);
        }
        else{
            available = false ;
            return -1;
        }
    }

    public void WritetoCache(int adr , int val){
        cach[getindextag(adr)[0]].setValue(getindextag(adr)[1], val);
    }


    @Override
    public void handleEvent(Event source_event){
        switch(source_event.getEventType()){
            case MemoryResponse:
                MemoryResponseEvent handle_event_response = (MemoryResponseEvent) source_event;
                WritetoCache(this.miss_address, handle_event_response.getValue());
                break;
            
            case MemoryRead:
                MemoryReadEvent handle_event_read = (MemoryReadEvent) source_event;
                int data = cacheRead(handle_event_read.getAddressToReadFrom());
                if(!available){
                    handle_event_read.setEventTime(Clock.getCurrentTime() + Configuration.mainMemoryLatency + 1);
                    this.miss_address = handle_event_read.getAddressToReadFrom();
                    Simulator.getEventQueue().addEvent(handle_event_read);
                    Simulator.getEventQueue().addEvent(new MemoryReadEvent(Clock.getCurrentTime() + Configuration.mainMemoryLatency,this,containingProcessor.getMainMemory(),this.miss_address));
                }
                else{
                    Simulator.getEventQueue().addEvent(new MemoryResponseEvent(Clock.getCurrentTime() + this.latency,this,handle_event_read.getRequestingElement(),data));
                }
                break ;
            case MemoryWrite:
                MemoryWriteEvent handle_event_write = (MemoryWriteEvent) source_event;
                WritetoCache(handle_event_write.getAddressToWriteTo(), handle_event_write.getValue());
                containingProcessor.getMainMemory().setWord(handle_event_write.getAddressToWriteTo(), handle_event_write.getValue());
                Simulator.getEventQueue().addEvent(new ExecutionCompleteEvent(Clock.getCurrentTime() + Configuration.mainMemoryLatency,containingProcessor.getMainMemory(),handle_event_write.getRequestingElement()));
                break;
            default :
                break;
        }
    }
}